package com.example.ktb3community.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private final String code;
    private final String message;

    public static ErrorResponseDto from (CustomException e) {
        return new ErrorResponseDto (e.getErrorCode().getCode(), e.getMessage());
    }

    public static ErrorResponseDto of (String code, String message) {
        return new ErrorResponseDto (code, message);
    }
}
