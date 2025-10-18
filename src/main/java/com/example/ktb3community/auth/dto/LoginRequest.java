package com.example.ktb3community.auth.dto;

import com.example.ktb3community.common.constants.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest (
        @Schema(description = "이메일", example = "lydbsdud@gmail.com")
        @NotBlank @Email
        String email,
        @Schema(description = "비밀번호", example = "Dbsdud1105!")
        @NotBlank
        @Pattern(
                regexp = ValidationConstant.PASSWORD_PATTERN
        )
        String password
){
}
