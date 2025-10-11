package com.example.ktb3community.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
public class ApiResponse<T> {
    public static final String DEFAULT_SUCCESS_MESSAGE = "OK";

    String message;
    T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().message(DEFAULT_SUCCESS_MESSAGE).data(data).build();
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder().message(DEFAULT_SUCCESS_MESSAGE).build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder().message(message).data(data).build();
    }

    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder().message(message).build();
    }
}