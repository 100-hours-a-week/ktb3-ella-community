package com.example.ktb3community.post.controller;

import com.example.ktb3community.common.doc.ApiCommonErrorResponses;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성", description = "사용자가 새 게시글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")

    })
    @ApiCommonErrorResponses
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResult<CreatePostResponse>> createPost(
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId,
            @Valid @RequestBody CreatePostRequest createPostRequest) {
        CreatePostResponse createPostResponse = postService.createPost(userId, createPostRequest);
        return ResponseEntity.ok(ApiResult.ok(createPostResponse));
    }

    @Operation(summary = "게시글 목록 조회", description = "페이지네이션과 정렬 옵션을 사용하여 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "pageSize는 1~20 사이만 허용합니다."),
            @ApiResponse(responseCode = "400", description = "page는 1부터 허용합니다.")
    })
    @ApiCommonErrorResponses
    @GetMapping
    public ResponseEntity<ApiResult<PageResponse<PostListResponse>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "new") PostSort sort
    ) {
        if (page < 1) throw new BusinessException(ErrorCode.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 20) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }
        PageResponse<PostListResponse> pageResponse = postService.getPostList(page, pageSize, sort);
        return ResponseEntity.ok(ApiResult.ok(pageResponse));
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다."),
    })
    @ApiCommonErrorResponses
    @GetMapping("/{postId}/{userId}")
    public ResponseEntity<ApiResult<PostDetailResponse>> getPostDetail(
            @Parameter(description = "게시글 id", example = "1") @PathVariable Long postId,
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId) {
        PostDetailResponse postDetailResponse = postService.getPostDetail(postId, userId);
        return ResponseEntity.ok(ApiResult.ok(postDetailResponse));
    }

    @Operation(summary = "게시글 수정", description = "특정 게시글의 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @PutMapping("/{postId}/{userId}")
    public ResponseEntity<ApiResult<CreatePostResponse>> updatePost(
            @Parameter(description = "게시글 id", example = "1") @PathVariable Long postId,
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId,
            @Valid @RequestBody CreatePostRequest createPostRequest
    ) {
        CreatePostResponse createPostResponse = postService.updatePost(postId, userId, createPostRequest);
        return ResponseEntity.ok(ApiResult.ok(createPostResponse));
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @DeleteMapping("/{postId}/{userId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 id", example = "1") @PathVariable Long postId,
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId
    ) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
