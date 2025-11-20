package com.example.ktb3community.post.controller;

import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.common.doc.ApiCommonErrorResponses;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.post.dto.LikeResponse;
import com.example.ktb3community.post.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시글 좋아요", description = "사용자가 게시글에 좋아요를 누릅니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResult<LikeResponse>> likePost(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "게시글 id", example = "1") @PathVariable Long postId) {
        Long userId = customUserDetails.getId();
        LikeResponse likeResponse = likeService.likePost(postId, userId);
        return ResponseEntity.ok(ApiResult.ok(likeResponse));
    }

    @Operation(summary = "게시글 좋아요 취소", description = "사용자가 게시글에 눌렀던 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResult<LikeResponse>> unlikePost(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "게시글 id", example = "1") @PathVariable Long postId) {
        Long userId = customUserDetails.getId();
        LikeResponse likeResponse = likeService.unlikePost(postId, userId);
        return ResponseEntity.ok(ApiResult.ok(likeResponse));
    }
}
