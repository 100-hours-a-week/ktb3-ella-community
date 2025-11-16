package com.example.ktb3community.s3.service;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.s3.dto.PresignUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.s3.upload-exp-minutes:10}")
    private long uploadExpMinutes;

    public PresignUploadResponse presignUpload(String originalFileName, String contentType) {
        String safeName = Paths.get(originalFileName == null ? "" : originalFileName).getFileName().toString();
        if (safeName.isBlank()) throw new BusinessException(ErrorCode.FILE_NAME_IS_NOT_BLANK);
        String key = "images/" + UUID.randomUUID() + "_" + safeName;

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType == null || contentType.isBlank() ? "image/png" : contentType)
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(Duration.ofMinutes(uploadExpMinutes))
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignReq);

        return new PresignUploadResponse(
                presigned.url().toString(),
                key,
                presigned.signedHeaders()
        );
    }

    public void deleteImageIfChanged(String previousImageUrl, String updatedImageUrl) {
        if (previousImageUrl == null || previousImageUrl.isBlank()) {
            return;
        }
        if (Objects.equals(previousImageUrl, updatedImageUrl)) {
            return;
        }
        deleteImage(previousImageUrl);
    }

    public void deleteImage(String imageUrl) {
        String key = extractKey(imageUrl);
        if (key == null) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new BusinessException(ErrorCode.S3_DELETE_FAILED);
        }
    }

    private String extractKey(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        String candidate = imageUrl.trim();
        if (candidate.isBlank()) {
            return null;
        }
        if (candidate.contains("://")) {
            try {
                candidate = URI.create(candidate).getPath();
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_IMG_URL);
            }
        }
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        if (candidate.startsWith("/")) {
            candidate = candidate.substring(1);
        }
        if (!candidate.startsWith("images/")) {
            return null;
        }
        return candidate;
    }
}
