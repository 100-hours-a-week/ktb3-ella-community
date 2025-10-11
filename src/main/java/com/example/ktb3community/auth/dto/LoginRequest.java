package com.example.ktb3community.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest (
        @Schema(description = "이메일", example = "lydbsdud@gmail.com")
        @NotBlank @Email
        String email,
        @Schema(description = "비밀번호", example = "Dbsdud1105!")
        @NotBlank @Size(min = 8, max = 20)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[~!@#$%^&*()_\\-+={}\\[\\]|\\\\:;\"'<>,.?/]).{8,20}$",
                message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
        )
        String password
){
}
