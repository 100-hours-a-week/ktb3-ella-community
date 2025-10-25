package com.example.ktb3community.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest (
        @Schema(description = "댓글 내용", example = "댓글 내용입니다")
        @NotBlank
        String content
)
{ }
