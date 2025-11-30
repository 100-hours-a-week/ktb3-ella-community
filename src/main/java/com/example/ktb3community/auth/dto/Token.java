package com.example.ktb3community.auth.dto;

public record Token (
        String accessToken,
        String refreshToken
){}
