package com.example.ktb3community.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank @Size(max = 26)
        String title,
        @NotBlank
        String content,
        String postImageUrl
) {
}
