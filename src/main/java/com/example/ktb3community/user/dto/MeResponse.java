package com.example.ktb3community.user.dto;

public record MeResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl
) {
}
