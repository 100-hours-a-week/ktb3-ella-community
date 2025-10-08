package com.example.ktb3community.exception;

public class BusinessException extends CustomException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
