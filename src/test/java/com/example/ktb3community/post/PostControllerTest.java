package com.example.ktb3community.post;

import com.example.ktb3community.WithMockCustomUser;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.CursorResponse;
import com.example.ktb3community.post.controller.PostController;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.service.PostService;
import com.example.ktb3community.post.service.PostViewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static com.example.ktb3community.TestFixtures.POST_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    PostService postService;

    @MockitoBean
    PostViewService postViewService;

    @Test
    @DisplayName("[201] 게시글 생성 성공")
    @WithMockCustomUser(id = USER_ID)
    void createPost_201_success() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Title", "Content", "http://img.url");
        CreatePostResponse response = new CreatePostResponse(POST_ID);

        given(postService.createPost(eq(USER_ID), any(CreatePostRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.postId").value(POST_ID));
    }

    @Test
    @DisplayName("[422] 제목이 없으면 유효성 검사 실패")
    @WithMockCustomUser(id = USER_ID)
    void createPost_422_invalidInput() throws Exception {
        CreatePostRequest invalidRequest = new CreatePostRequest("", "Content", "img");

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("[200] 게시글 목록 조회 성공")
    @WithMockCustomUser(id = USER_ID)
    void list_200_success() throws Exception {
        CursorResponse<PostListResponse> response =
                new CursorResponse<>(Collections.emptyList(), null, null, false);

        given(postViewService.getPostList(
                any(), any(), anyInt(), any(PostSort.class))
        ).willReturn(response);

        mockMvc.perform(get("/posts")
                        .param("pageSize", "10")
                        .param("sort", PostSort.LIKE.name())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("[400] pageSize가 범위를 벗어나면(21) 예외 발생")
    @WithMockCustomUser(id = USER_ID)
    void list_400_invalidPageSize() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("page", "1")
                        .param("pageSize", "21") // 21개 요청
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PAGE_SIZE.getCode()));
    }

    @Test
    @DisplayName("[200] 게시글 상세 조회 성공")
    @WithMockCustomUser(id = USER_ID)
    void getPostDetail_200_success() throws Exception {
        PostDetailResponse response = new PostDetailResponse(POST_ID, "Title", "Content", null, null, 0, 0, 0, false, null, null);

        given(postViewService.getPostDetail(POST_ID, USER_ID)).willReturn(response);

        mockMvc.perform(get("/posts/{postId}", POST_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(POST_ID))
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    @DisplayName("[200] 게시글 수정 성공")
    @WithMockCustomUser(id = USER_ID)
    void updatePost_200_success() throws Exception {
        CreatePostRequest request = new CreatePostRequest("New Title", "New Content", "img");
        CreatePostResponse response = new CreatePostResponse(POST_ID);

        given(postService.updatePost(eq(POST_ID), eq(USER_ID), any(CreatePostRequest.class)))
                .willReturn(response);

        mockMvc.perform(put("/posts/{postId}", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[204] 게시글 삭제 성공")
    @WithMockCustomUser(id = USER_ID)
    void deletePost_204_success() throws Exception {
        mockMvc.perform(delete("/posts/{postId}", POST_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(POST_ID, USER_ID);
    }
}
