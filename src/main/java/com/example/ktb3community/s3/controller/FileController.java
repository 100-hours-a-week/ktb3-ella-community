package com.example.ktb3community.s3.controller;

import com.example.ktb3community.common.doc.ApiCommonErrorResponses;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.s3.dto.PresignUploadResponse;
import com.example.ktb3community.s3.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "파일 업로드용 사전 서명 URL 발급", description = "파일 업로드용 사전 서명 URL을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "파일 이름이 비어있습니다.")
    })
    @ApiCommonErrorResponses
    @GetMapping("/uploads/presigned-url")
    public ResponseEntity<ApiResult<PresignUploadResponse>> presignUpload(
            @Parameter(description = "파일 이름", example = "ella.png") @RequestParam String fileName,
            @RequestParam(required = false, defaultValue = "image/png") String contentType
    ) {
        PresignUploadResponse presignUploadResponse = fileService.presignUpload(fileName, contentType);
        return ResponseEntity.ok(ApiResult.ok(presignUploadResponse));
    }
}
