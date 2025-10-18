package com.example.ktb3community.user.dto;

import com.example.ktb3community.common.constants.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
        @Schema(description = "닉네임", example = "ella")
        @Size(max = ValidationConstant.NICKNAME_MAX_LENGTH)
        @Pattern(
                regexp = ValidationConstant.NICKNAME_PATTERN_NO_SPACE
        )
        String nickname,
        @Schema(description = "이미지 URL", example = "https://ella.png")
        String profileImageUrl
) {}
