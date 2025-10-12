package com.example.ktb3community.post.dto;

public record LikeResponse(
        long likeCount,
        long viewCount,
        long commentCount
) {
}
