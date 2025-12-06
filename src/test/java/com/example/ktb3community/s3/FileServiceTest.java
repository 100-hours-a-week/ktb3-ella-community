package com.example.ktb3community.s3;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.s3.dto.PresignUploadResponse;
import com.example.ktb3community.s3.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock S3Presigner s3Presigner;
    @Mock S3Client s3Client;

    @InjectMocks
    FileService fileService;

    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(fileService, "uploadExpMinutes", 10L);
    }


    @Test
    @DisplayName("presignUpload: 유효한 파일명과 타입이면 Presigned URL을 발급한다")
    void presignUpload_success() throws MalformedURLException {
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        URL mockUrl = new URL("https://s3.aws.com/test-bucket/images/uuid_test.jpg");

        PresignedPutObjectRequest presignedResponse = mock(PresignedPutObjectRequest.class);
        given(presignedResponse.url()).willReturn(mockUrl);
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presignedResponse);

        PresignUploadResponse response = fileService.presignUpload(fileName, contentType);

        assertThat(response.uploadUrl()).isEqualTo(mockUrl.toString());
        assertThat(response.key()).startsWith("images/");
        assertThat(response.key()).endsWith("_" + fileName);
    }

    @Test
    @DisplayName("presignUpload: 지원하지 않는 Content-Type이면 예외 발생")
    void presignUpload_invalidType_throws() {
        String fileName = "test.txt";
        String contentType = "text/plain";

        assertThatThrownBy(() -> fileService.presignUpload(fileName, contentType))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_TYPE_NOT_ALLOWED);
    }

    @ParameterizedTest
    @DisplayName("presignUpload: 파일명이 비어있으면 예외 발생")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void presignUpload_blankName_throws(String invalid) {
        assertThatThrownBy(() -> fileService.presignUpload(invalid, "image/png"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NAME_IS_NOT_BLANK);
    }

    @Test
    @DisplayName("deleteImageIfChanged: 이미지가 변경되었으면 기존 이미지를 삭제한다")
    void deleteImageIfChanged_success() {
        String oldUrl = "https://bucket.s3.com/images/old.jpg";
        String newUrl = "https://bucket.s3.com/images/new.jpg";

        fileService.deleteImageIfChanged(oldUrl, newUrl);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("deleteImageIfChanged: 기존 이미지가 없거나(null/blank) 변경되지 않았으면 삭제하지 않는다")
    void deleteImageIfChanged_skip() {
        fileService.deleteImageIfChanged(null, "new.jpg");
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));

        fileService.deleteImageIfChanged("same.jpg", "same.jpg");
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("deleteImage: 전체 URL에서 Key를 추출하여 S3 삭제를 요청한다")
    void deleteImage_fullUrl_success() {
        String expectedKey = "images/test-uuid_image.jpg";
        String fullUrl = "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + expectedKey;

        fileService.deleteImage(fullUrl);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());

        DeleteObjectRequest request = captor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.key()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("deleteImage: 상대 경로(/images/...)가 입력되어도 삭제 가능하다")
    void deleteImage_relativePath_success() {
        String relativePath = "/images/test.jpg";

        fileService.deleteImage(relativePath);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo("images/test.jpg");
    }

    @Test
    @DisplayName("deleteImage: 'images/'로 시작하지 않는 Key는 예외 발생 (잘못된 경로)")
    void deleteImage_invalidKey_throws() {
        String invalidUrl = "https://s3.com/bucket/files/test.jpg";

        Throwable thrown = catchThrowable(() ->  fileService.deleteImage(invalidUrl));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_S3_KEY);
    }

    @Test
    @DisplayName("deleteImage: URL 형식이 잘못되면 예외 발생")
    void deleteImage_malformedUrl_throws() {
        String badUrl = "http://bad url with spaces";

        Throwable thrown = catchThrowable(() ->  fileService.deleteImage(badUrl));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_IMG_URL);
    }

    @Test
    @DisplayName("deleteImage: S3 클라이언트 에러 발생 시 BusinessException으로 래핑한다")
    void deleteImage_s3Exception_throws() {
        String url = "https://bucket.com/images/valid.jpg";

        doThrow(S3Exception.builder().message("AWS Error").build())
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        Throwable thrown = catchThrowable(() ->  fileService.deleteImage(url));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.S3_DELETE_FAILED);
    }

    @ParameterizedTest
    @DisplayName("deleteImage: URL이 null이거나 공백이면 INVALID_IMG_URL 예외 발생")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void deleteImage_nullOrBlank_throws(String invalid) {
        Throwable thrown = catchThrowable(() -> fileService.deleteImage(invalid));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_IMG_URL);

        verifyNoInteractions(s3Client);
    }

}