package com.example.ktb3community.post.controller;

import com.example.ktb3community.common.response.ApiResponse;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(@Valid @RequestBody CreatePostRequest createPostRequest) {
        CreatePostResponse createPostResponse = postService.createPost(createPostRequest);
        return ResponseEntity.ok(ApiResponse.ok(createPostResponse));
    }
}
