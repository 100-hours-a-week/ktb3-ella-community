package com.example.ktb3community.auth.dto;

public record TokenDto(
        String accessToken,
        String refreshToken
){}
