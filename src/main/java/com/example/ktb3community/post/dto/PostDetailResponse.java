package com.example.ktb3community.post.dto;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.common.pagination.PageResponse;

import java.time.Instant;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        String postImageUrl,
        Author author,
        long likeCount,
        long viewCount,
        long commentCount,
        Instant createdAt,
        PageResponse<CommentResponse> comments
) {
}
