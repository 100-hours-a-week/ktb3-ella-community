package com.example.ktb3community.comment;

import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.comment.controller.CommentController;
import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.dto.CreateCommentRequest;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static com.example.ktb3community.TestFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    CommentService commentService;

    @BeforeEach
    void setUp() {
        User mockUser = User.builder()
                .id(USER_ID)
                .email("test@email.com")
                .passwordHash("encoded")
                .nickname("test")
                .role(Role.ROLE_USER)
                .build();

        CustomUserDetails principal = CustomUserDetails.from(mockUser);

        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("[201] 댓글 생성 성공")
    void createComment_201_success() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Nice Post!");
        CommentResponse response = new CommentResponse(COMMENT_ID, "Nice Post!", null, null);

        given(commentService.createComment(eq(POST_ID), eq(USER_ID), any(CreateCommentRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/posts/{postId}/comments", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.commentId").value(COMMENT_ID))
                .andExpect(jsonPath("$.data.content").value("Nice Post!"));
    }

    @Test
    @DisplayName("[422] 내용이 없는 댓글 생성 시 유효성 검사 실패")
    void createComment_422_invalidContent() throws Exception {
        CreateCommentRequest invalidRequest = new CreateCommentRequest("");

        mockMvc.perform(post("/posts/{postId}/comments", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("[200] 댓글 목록 조회 성공")
    void getComments_200_success() throws Exception {
        PageResponse<CommentResponse> response = new PageResponse<>(Collections.emptyList(), 1, 10, 0);

        given(commentService.getCommentList(POST_ID, 1)).willReturn(response);

        mockMvc.perform(get("/posts/{postId}/comments", POST_ID)
                        .param("page", "1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[400] 페이지 번호가 1 미만이면 예외 발생")
    void getComments_400_invalidPage() throws Exception {
        mockMvc.perform(get("/posts/{postId}/comments", POST_ID)
                        .param("page", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PAGE.getCode()));
    }

    @Test
    @DisplayName("[200] 댓글 수정 성공")
    void updateComment_200_success() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Updated Content");
        CommentResponse response = new CommentResponse(COMMENT_ID, "Updated Content", null, null);

        given(commentService.updateComment(eq(COMMENT_ID), eq(USER_ID), any(CreateCommentRequest.class)))
                .willReturn(response);

        mockMvc.perform(put("/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Updated Content"));
    }

    @Test
    @DisplayName("[204] 댓글 삭제 성공")
    void deleteComment_204_success() throws Exception {
        mockMvc.perform(delete("/comments/{commentId}", COMMENT_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(COMMENT_ID, USER_ID);
    }
}
