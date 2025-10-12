package com.example.ktb3community.post.controller;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.common.response.ApiResponse;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성")
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(@PathVariable Long userId, @Valid @RequestBody CreatePostRequest createPostRequest) {
        CreatePostResponse createPostResponse = postService.createPost(userId, createPostRequest);
        return ResponseEntity.ok(ApiResponse.ok(createPostResponse));
    }

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListResponse>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "new") PostSort sort
    ) {
        if (page < 1) throw new BusinessException(ErrorCode.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 20) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }
        PageResponse<PostListResponse> pageResponse = postService.getPostList(page, pageSize, sort);
        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId) {
        PostDetailResponse postDetailResponse = postService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.ok(postDetailResponse));
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{postId}/{userId}")
    public ResponseEntity<ApiResponse<CreatePostResponse>> updatePost(
            @PathVariable Long postId, @PathVariable Long userId, @Valid @RequestBody CreatePostRequest createPostRequest
    ) {
        CreatePostResponse createPostResponse = postService.updatePost(postId, userId, createPostRequest);
        return ResponseEntity.ok(ApiResponse.ok(createPostResponse));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}/{userId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId, @PathVariable Long userId
    ) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
