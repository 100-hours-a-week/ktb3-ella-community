package com.example.ktb3community.user.exception;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.CustomException;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}

