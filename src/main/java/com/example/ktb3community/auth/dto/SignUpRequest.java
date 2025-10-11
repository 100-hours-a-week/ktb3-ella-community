package com.example.ktb3community.auth.dto;

import jakarta.validation.constraints.*;

public record SignUpRequest (
    @NotBlank @Email
    String email,
    @NotBlank @Size(min = 8, max = 20)
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[~!@#$%^&*()_\\-+={}\\[\\]|\\\\:;\"'<>,.?/]).{8,20}$",
        message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    String password,
    @NotBlank @Size(max=10)
    @Pattern(
        regexp = "^\\S+$",
        message = "닉네임에 공백을 포함할 수 없습니다."
    )
    String nickname,
    @NotBlank
    String profileImageUrl
) {}
