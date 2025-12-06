package com.example.ktb3community.auth;

import com.example.ktb3community.auth.controller.AuthController;
import com.example.ktb3community.auth.controller.TokenResponder;
import com.example.ktb3community.auth.dto.AuthResponse;
import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.dto.TokenDto;
import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.auth.service.AuthService;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.ktb3community.TestFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    TokenResponder tokenResponder;

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
    @DisplayName("[201] 회원가입 성공")
    void signup_201_success() throws Exception {
        SignUpRequest request = new SignUpRequest("test@email.com", "Password1234!", "nickname", "img");
        TokenDto tokenDto = new TokenDto(ACCESS_TOKEN, REFRESH_TOKEN);

        AuthResponse authResponse = new AuthResponse(ACCESS_TOKEN);
        ResponseEntity<ApiResult<AuthResponse>> responseEntity = ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.ok(authResponse));

        given(authService.signup(any(SignUpRequest.class))).willReturn(tokenDto);
        given(tokenResponder.success(eq(tokenDto), any(HttpServletResponse.class), eq(HttpStatus.CREATED)))
                .willReturn(responseEntity);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN));
    }

    @Test
    @DisplayName("[200] 로그인 성공")
    void login_200_success() throws Exception {
        LoginRequest request = new LoginRequest("test@email.com", "Password1234!");
        TokenDto tokenDto = new TokenDto(ACCESS_TOKEN, REFRESH_TOKEN);

        AuthResponse authResponse = new AuthResponse(ACCESS_TOKEN);
        ResponseEntity<ApiResult<AuthResponse>> responseEntity = ResponseEntity
                .ok(ApiResult.ok(authResponse));

        given(authService.login(any(LoginRequest.class))).willReturn(tokenDto);
        given(tokenResponder.success(eq(tokenDto), any(HttpServletResponse.class), eq(HttpStatus.OK)))
                .willReturn(responseEntity);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN));
    }

    @Test
    @DisplayName("[401] 리프레시 토큰 쿠키가 없으면 예외 발생")
    void refresh_401_missingCookie() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()));
    }

    @Test
    @DisplayName("[204] 로그아웃 성공")
    void logout_204_success() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refresh_token", REFRESH_TOKEN))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(authService).logout(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("[401] 로그아웃 시 리프레시 토큰이 없으면 예외 발생")
    void logout_401_missingCookie() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()));
    }
}
