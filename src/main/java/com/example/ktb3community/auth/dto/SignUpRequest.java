package com.example.ktb3community.auth.dto;

import com.example.ktb3community.common.constants.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record SignUpRequest (
    @Schema(description = "이메일", example = "lydbsdud@gmail.com")
    @NotBlank @Email
    String email,
    @Schema(description = "비밀번호", example = "Dbsdud1105!")
    @NotBlank
    @Pattern(
            regexp = ValidationConstant.PASSWORD_PATTERN
    )
    String password,
    @Schema(description = "닉네임", example = "ella")
    @NotBlank
    @Size(max = ValidationConstant.NICKNAME_MAX_LENGTH)
    @Pattern(
            regexp = ValidationConstant.NICKNAME_PATTERN_NO_SPACE
    )
    String nickname,
    @Schema(description = "이미지 URL", example = "https://ella.png")
    @NotBlank
    String profileImageUrl
) {}
