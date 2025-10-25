package com.example.ktb3community.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "ErrorResponseDto", description = "공통 에러 래퍼")
public class ErrorResponseDto {
    @Schema(description = "결과 코드", example = "USER_NOT_FOUND")
    private final String code;
    @Schema(description = "메시지", example = "사용자를 찾을 수 없습니다.")
    private final String message;

    public static ErrorResponseDto from (CustomException e) {
        return new ErrorResponseDto (e.getErrorCode().getCode(), e.getMessage());
    }

    public static ErrorResponseDto of (String code, String message) {
        return new ErrorResponseDto (code, message);
    }
}
