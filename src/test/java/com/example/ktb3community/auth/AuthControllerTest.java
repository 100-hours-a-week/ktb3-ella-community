package com.example.ktb3community.auth;

import com.example.ktb3community.auth.controller.AuthController;
import com.example.ktb3community.auth.controller.TokenResponder;
import com.example.ktb3community.auth.dto.AuthResponse;
import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.dto.Token;
import com.example.ktb3community.auth.service.AuthService;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.response.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.ktb3community.TestFixtures.ACCESS_TOKEN;
import static com.example.ktb3community.TestFixtures.REFRESH_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean
    TokenResponder tokenResponder;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("user", null, null));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("[201] 회원가입 성공")
    void signup_201_success() throws Exception {
        SignUpRequest request = new SignUpRequest("test@email.com", "Password1234!", "nickname", "img");
        Token token = new Token(ACCESS_TOKEN, REFRESH_TOKEN);

        AuthResponse authResponse = new AuthResponse(ACCESS_TOKEN);
        ResponseEntity<ApiResult<AuthResponse>> responseEntity = ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.ok(authResponse));

        given(authService.signup(any(SignUpRequest.class))).willReturn(token);
        given(tokenResponder.success(eq(token), any(HttpServletResponse.class), eq(HttpStatus.CREATED)))
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
        Token token = new Token(ACCESS_TOKEN, REFRESH_TOKEN);

        AuthResponse authResponse = new AuthResponse(ACCESS_TOKEN);
        ResponseEntity<ApiResult<AuthResponse>> responseEntity = ResponseEntity
                .ok(ApiResult.ok(authResponse));

        given(authService.login(any(LoginRequest.class))).willReturn(token);
        given(tokenResponder.success(eq(token), any(HttpServletResponse.class), eq(HttpStatus.OK)))
                .willReturn(responseEntity);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN));
    }

    @Test
    @DisplayName("[200] 토큰 갱신 성공 (쿠키 필수)")
    void refresh_200_withCookie() throws Exception {
        Token newToken = new Token("new.access", "new.refresh");
        AuthResponse authResponse = new AuthResponse("new.access");
        ResponseEntity<ApiResult<AuthResponse>> responseEntity = ResponseEntity
                .ok(ApiResult.ok(authResponse));

        given(authService.refresh(REFRESH_TOKEN)).willReturn(newToken);
        given(tokenResponder.success(eq(newToken), any(HttpServletResponse.class), eq(HttpStatus.OK)))
                .willReturn(responseEntity);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", REFRESH_TOKEN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new.access"));
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
