package com.example.ktb3community.exception;

import com.example.ktb3community.common.error.ErrorCode;

public class BusinessException extends CustomException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
