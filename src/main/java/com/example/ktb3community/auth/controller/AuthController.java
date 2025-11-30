package com.example.ktb3community.auth.controller;

import com.example.ktb3community.auth.dto.AuthResponse;
import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.dto.Token;
import com.example.ktb3community.auth.service.AuthService;
import com.example.ktb3community.common.doc.ApiCommonErrorResponses;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.util.CookieUtil;
import com.example.ktb3community.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.example.ktb3community.common.response.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final TokenResponder tokenResponder;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "OK"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다.")

    })
    @ApiCommonErrorResponses
    @PostMapping("/signup")
    public ResponseEntity<ApiResult<AuthResponse>> signup(
            @Valid @RequestBody SignUpRequest signUpRequest,
            HttpServletResponse response) {
        Token token = authService.signup(signUpRequest);
        return tokenResponder.success(token, response, HttpStatus.CREATED);
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @PostMapping("/login")
    public ResponseEntity<ApiResult<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Token token = authService.login(loginRequest);
        return tokenResponder.success(token, response, HttpStatus.OK);
    }

    @Operation(summary = "토큰 갱신", description = "액세스 토큰, 리프레시 토큰을 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token", required=false) String refreshJwt,
            HttpServletResponse response
    ) {
        if (refreshJwt == null || refreshJwt.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        Token token = authService.refresh(refreshJwt);
        return tokenResponder.success(token, response, HttpStatus.OK);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required=false) String refreshJwt,
            HttpServletResponse response
    ) {
        if (refreshJwt == null || refreshJwt.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        authService.logout(refreshJwt);
        CookieUtil.removeRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
