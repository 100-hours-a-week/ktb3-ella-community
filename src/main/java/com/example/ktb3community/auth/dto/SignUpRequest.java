package com.example.ktb3community.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record SignUpRequest (
    @Schema(description = "이메일", example = "lydbsdud@gmail.com")
    @NotBlank @Email
    String email,
    @Schema(description = "비밀번호", example = "Dbsdud1105!")
    @NotBlank @Size(min = 8, max = 20)
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[~!@#$%^&*()_\\-+={}\\[\\]|\\\\:;\"'<>,.?/]).{8,20}$",
        message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    String password,
    @Schema(description = "닉네임", example = "ella")
    @NotBlank @Size(max=10)
    @Pattern(
        regexp = "^\\S+$",
        message = "닉네임에 공백을 포함할 수 없습니다."
    )
    String nickname,
    @Schema(description = "이미지 URL", example = "https://ella.png")
    @NotBlank
    String profileImageUrl
) {}
