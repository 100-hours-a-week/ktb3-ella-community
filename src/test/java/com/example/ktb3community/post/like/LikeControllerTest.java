package com.example.ktb3community.post.like;

import com.example.ktb3community.WithMockCustomUser;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.controller.LikeController;
import com.example.ktb3community.post.dto.LikeResponse;
import com.example.ktb3community.post.service.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.ktb3community.TestFixtures.POST_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeController.class)
class LikeControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean
    LikeService likeService;

    @Test
    @DisplayName("[200] 게시글 좋아요 성공")
    @WithMockCustomUser(id = USER_ID)
    void likePost_200_success() throws Exception {
        LikeResponse response = new LikeResponse(10L, 5L, 3L);

        given(likeService.likePost(POST_ID, USER_ID)).willReturn(response);

        mockMvc.perform(post("/posts/{postId}/likes", POST_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(10L))
                .andExpect(jsonPath("$.data.viewCount").value(5L))
                .andExpect(jsonPath("$.data.commentCount").value(3L));

        verify(likeService).likePost(POST_ID, USER_ID);
    }

    @Test
    @DisplayName("[404] 존재하지 않는 게시글에 좋아요 시도 시 예외 발생")
    @WithMockCustomUser(id = USER_ID)
    void likePost_404_postNotFound() throws Exception {
        given(likeService.likePost(POST_ID, USER_ID))
                .willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        mockMvc.perform(post("/posts/{postId}/likes", POST_ID)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.POST_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("[200] 게시글 좋아요 취소 성공")
    @WithMockCustomUser(id = USER_ID)
    void unlikePost_200_success() throws Exception {
        LikeResponse response = new LikeResponse(9L, 5L, 3L);

        given(likeService.unlikePost(POST_ID, USER_ID)).willReturn(response);

        mockMvc.perform(delete("/posts/{postId}/likes", POST_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(9L));

        verify(likeService).unlikePost(POST_ID, USER_ID);
    }
}
