package com.example.ktb3community.post.exception;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.CustomException;

public class PostNotFoundException extends CustomException {
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}

