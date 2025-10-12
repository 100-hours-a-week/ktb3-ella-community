package com.example.ktb3community.comment.dto;

import com.example.ktb3community.post.dto.Author;

import java.time.Instant;

public record CommentResponse(
    long commentId,
    String content,
    Author author,
    Instant createAt
){ }