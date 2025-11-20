package com.example.ktb3community.comment.controller;

import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.dto.CreateCommentRequest;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.doc.ApiCommonErrorResponses;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "댓글 생성", description = "특정 게시글에 댓글을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다.")
    })
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResult<CommentResponse>> createComment(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "게시글 id", example = "1") @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest createCommentRequest){
        Long userId = customUserDetails.getId();
        CommentResponse commentResponse = commentService.createComment(postId, userId, createCommentRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.ok(commentResponse));
    }

    @Operation(summary = "댓글 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 페이지 번호입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다.")
    })
    @ApiCommonErrorResponses
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResult<PageResponse<CommentResponse>>> getComments(
            @Parameter(description = "게시글 id", example = "1") @PathVariable Long postId,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") Integer page) {
        if(page < 1){throw new BusinessException(ErrorCode.INVALID_PAGE);}
        PageResponse<CommentResponse> comments = commentService.getCommentList(postId, page);
        return ResponseEntity.ok(ApiResult.ok(comments));
    }

    @Operation(summary = "댓글 수정", description = "특정 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글입니다."),
    })
    @ApiCommonErrorResponses
    @PutMapping("comments/{commentId}")
    public ResponseEntity<ApiResult<CommentResponse>> updateComment(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "댓글 id", example = "1") @PathVariable Long commentId,
            @Valid @RequestBody CreateCommentRequest createCommentRequest){
        Long userId = customUserDetails.getId();
        CommentResponse commentResponse = commentService.updateComment(commentId, userId, createCommentRequest);
        return ResponseEntity.ok(ApiResult.ok(commentResponse));
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글입니다."),
    })
    @ApiCommonErrorResponses
    @DeleteMapping("comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "댓글 id", example = "1")  @PathVariable Long commentId){
        Long userId = customUserDetails.getId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
