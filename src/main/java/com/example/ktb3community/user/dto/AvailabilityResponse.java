package com.example.ktb3community.user.dto;

public record AvailabilityResponse(
        Boolean emailAvailable,
        Boolean nicknameAvailable
) {
}
