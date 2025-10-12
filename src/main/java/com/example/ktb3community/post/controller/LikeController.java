package com.example.ktb3community.post.controller;

import com.example.ktb3community.common.response.ApiResponse;
import com.example.ktb3community.post.dto.LikeResponse;
import com.example.ktb3community.post.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시글 좋아요")
    @PostMapping("/{postId}/likes/{userId}")
    public ResponseEntity<ApiResponse<LikeResponse>> likePost(@PathVariable long postId, @PathVariable long userId) {
        LikeResponse likeResponse = likeService.likePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.ok(likeResponse));
    }

    @Operation(summary = "게시글 좋아요 취소")
    @DeleteMapping("/{postId}/likes/{userId}")
    public ResponseEntity<ApiResponse<LikeResponse>> unlikePost(@PathVariable long postId, @PathVariable long userId) {
        LikeResponse likeResponse = likeService.unlikePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.ok(likeResponse));
    }
}
