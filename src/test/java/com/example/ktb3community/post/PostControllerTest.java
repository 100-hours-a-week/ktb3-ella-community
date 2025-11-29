package com.example.ktb3community.post;

import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.post.controller.PostController;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.service.PostService;
import com.example.ktb3community.post.service.PostViewService;
import com.example.ktb3community.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @BeforeEach
    void setUp() {
        User mockUser = User.builder()
                .id(USER_ID)
                .email("test@email.com")
                .role(com.example.ktb3community.common.Role.ROLE_USER)
                .build();

        CustomUserDetails principal = CustomUserDetails.from(mockUser);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("[201] 게시글 생성 성공")
    void createPost_201() throws Exception {
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
    void createPost_422_invalid() throws Exception {
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
    void list_200() throws Exception {
        PageResponse<PostListResponse> response = new PageResponse<>(Collections.emptyList(), 1, 10, 0);

        given(postViewService.getPostList(anyInt(), anyInt(), any(PostSort.class)))
                .willReturn(response);

        mockMvc.perform(get("/posts")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("sort", "NEW")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[400] page가 1 미만이면 예외 발생")
    void list_400_invalidPage() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PAGE.getCode()));
    }

    @Test
    @DisplayName("[400] pageSize가 범위를 벗어나면(21) 예외 발생")
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
    void getPostDetail_200() throws Exception {
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
    void updatePost_200() throws Exception {
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
    void deletePost_204() throws Exception {
        mockMvc.perform(delete("/posts/{postId}", POST_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(POST_ID, USER_ID);
    }
}