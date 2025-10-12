package com.example.ktb3community.comment.exception;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.CustomException;

public class CommentNotFound extends CustomException {
    public CommentNotFound() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}