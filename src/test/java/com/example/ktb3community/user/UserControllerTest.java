package com.example.ktb3community.user;
import com.example.ktb3community.user.controller.UserController;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("getAvailability에서 이메일과 닉네임 파라미터가 모두 없으면 400 에러가 터져야 한다")
    void getAvailability_missingBothParams_throws() throws Exception {
        mockMvc.perform(get("/users/availability")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("updateMe에서 닉네임 형식이 맞지 않으면 422 에러가 터져야 한다")
    @WithMockUser
    void updateMe_validationNickname_throws() throws Exception {
        UpdateMeRequest invalidRequest = new UpdateMeRequest("invalid nickname", "https://img.url");

        mockMvc.perform(patch("/users/me")
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }
}