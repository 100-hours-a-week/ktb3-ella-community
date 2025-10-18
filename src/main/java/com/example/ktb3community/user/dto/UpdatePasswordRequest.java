package com.example.ktb3community.user.dto;

import com.example.ktb3community.common.constants.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordRequest(
        @Schema(description = "새 비밀번호", example = "Dbsdud1234!")
        @NotBlank
        @Pattern(
                regexp = ValidationConstant.PASSWORD_PATTERN
        )
        String newPassword
) {
}
