package com.example.ktb3community.user.domain.dto;

public record MeResponse(
        String email,
        String nickname,
        String profileImageUrl
) {
}
