package com.example.ktb3community.user;

import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.controller.UserController;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.dto.UpdatePasswordRequest;
import com.example.ktb3community.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

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
    @DisplayName("[200] 중복 확인 성공")
    void getAvailability_200_success() throws Exception {
        AvailabilityResponse response = new AvailabilityResponse(true, true);
        given(userService.getAvailability("email", null)).willReturn(response);

        mockMvc.perform(get("/users/availability")
                        .param("email", "email")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailAvailable").value(true));
    }

    @Test
    @DisplayName("[400] 파라미터가 둘 다 없으면 Bad Request")
    void getAvailability_400_missingParams() throws Exception {

        mockMvc.perform(get("/users/availability")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.MISSING_PARAMETER.getCode()));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("[500] 서버 내부 에러 발생")
    void getAvailability_500_internalError() throws Exception {
        given(userService.getAvailability(any(), any()))
                .willThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/users/availability")
                        .param("email", "test")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    @DisplayName("[200] 내 정보 조회 성공")
    void getMe_200_success() throws Exception {
        MeResponse response = new MeResponse("email", "nick", "img");
        given(userService.getMe(USER_ID)).willReturn(response);

        mockMvc.perform(get("/users/me")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("nick"));
    }

    @Test
    @DisplayName("[404] 존재하지 않는 유저 조회 시 404 Not Found")
    void getMe_404_notFound() throws Exception {
        given(userService.getMe(USER_ID))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/users/me")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("[204] 내 비밀번호 수정 성공")
    void updatePassword_204_success() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("NewPassword1234!");

        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).updatePassword(eq(USER_ID), any());

    }

    @Test
    @DisplayName("[422] @Valid 검증 실패 시 422")
    void updatePassword_422_validation() throws Exception {
        UpdatePasswordRequest invalidRequest = new UpdatePasswordRequest( "newPassword");

        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("[200] 내 정보 수정 성공")
    void updateMe_200_success() throws Exception {
        UpdateMeRequest request = new UpdateMeRequest("newNick", "newImg");
        MeResponse response = new MeResponse("email", "newNick", "newImg");

        given(userService.updateMe(eq(USER_ID), any(UpdateMeRequest.class)))
                .willReturn(response);

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("newNick"));
    }

    @Test
    @DisplayName("[422] @Valid 검증 실패 시 422")
    void updateMe_422_validation() throws Exception {
        UpdateMeRequest invalidRequest = new UpdateMeRequest("", "");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("[400] JSON 형식이 잘못된 경우 400 Bad Request")
    void updateMe_400_badJson() throws Exception {
        String brokenJson = "{ \"nickname\": \"test\", }";

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest()) // 400 확인
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST_BODY.getCode()));

        verifyNoInteractions(userService);
    }
    @Test
    @DisplayName("[204] 회원 탈퇴 성공")
    void withdrawMe_204_success() throws Exception {
        mockMvc.perform(delete("/users/me")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).withdrawMe(eq(USER_ID), any());
    }

    @Test
    @DisplayName("[403] 서비스에서 접근 거부 예외 발생 시 403 Forbidden")
    void withdrawMe_403_forbidden() throws Exception {
        doThrow(new BusinessException(ErrorCode.AUTH_FORBIDDEN))
                .when(userService).withdrawMe(eq(USER_ID), any());

        mockMvc.perform(delete("/users/me")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_FORBIDDEN.getCode()));
    }
}
