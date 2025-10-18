package com.example.ktb3community.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
public class ApiResult<T> {
    public static final String DEFAULT_SUCCESS_MESSAGE = "OK";

    String message;
    T data;

    public static <T> ApiResult<T> ok(T data) {
        return ApiResult.<T>builder().message(DEFAULT_SUCCESS_MESSAGE).data(data).build();
    }

    public static ApiResult<Void> ok() {
        return ApiResult.<Void>builder().message(DEFAULT_SUCCESS_MESSAGE).build();
    }

    public static <T> ApiResult<T> ok(String message, T data) {
        return ApiResult.<T>builder().message(message).data(data).build();
    }

    public static ApiResult<Void> ok(String message) {
        return ApiResult.<Void>builder().message(message).build();
    }
}