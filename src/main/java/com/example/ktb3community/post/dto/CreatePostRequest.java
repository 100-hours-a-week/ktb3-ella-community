package com.example.ktb3community.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @Schema(description = "게시글 제목", example = "KTB3 커뮤니티 만드는 방법")
        @NotBlank @Size(max = 26)
        String title,
        @Schema(description = "게시글 내용", example = "Springboot로 커뮤니티 만들기")
        @NotBlank
        String content,
        @Schema(description = "게시글 이미지", example = "https://post.png")
        String postImageUrl
) {
}
