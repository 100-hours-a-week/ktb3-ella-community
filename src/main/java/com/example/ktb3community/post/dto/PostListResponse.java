package com.example.ktb3community.post.dto;

import java.time.Instant;

public record PostListResponse(
        Long postId,
        String title,
        Author author,
        long likeCount,
        long viewCount,
        long commentCount,
        Instant createdAt
) {
}
