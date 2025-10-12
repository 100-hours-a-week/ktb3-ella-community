package com.example.ktb3community.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
        @Schema(description = "닉네임", example = "ella")
        @Pattern(
                regexp = "^\\S+$",
                message = "닉네임에 공백을 포함할 수 없습니다."
        )
        @Size(max=10)
        String nickname,
        @Schema(description = "이미지 URL", example = "https://ella.png")
        String profileImageUrl
) {}
