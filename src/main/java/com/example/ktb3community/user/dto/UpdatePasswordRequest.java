package com.example.ktb3community.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @Schema(description = "비밀번호", example = "Dbsdud1105!")
        @NotBlank @Size(min = 8, max = 20)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[~!@#$%^&*()_\\-+={}\\[\\]|\\\\:;\"'<>,.?/]).{8,20}$",
                message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
        )
        String newPassword
) {
}
