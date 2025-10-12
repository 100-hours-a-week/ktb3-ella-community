package com.example.ktb3community.comment.controller;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.dto.CreateCommentRequest;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.common.response.ApiResponse;
import com.example.ktb3community.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "댓글 생성")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest createCommentRequest){
        CommentResponse commentResponse = commentService.createComment(postId, createCommentRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(commentResponse));
    }

    @Operation(summary = "댓글 조회")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(@PathVariable Long postId, @RequestParam(defaultValue = "1") Integer page) {
        if(page < 1){throw new BusinessException(ErrorCode.INVALID_PAGE);}
        PageResponse<CommentResponse> comments = commentService.getCommentList(postId, page);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }
}
