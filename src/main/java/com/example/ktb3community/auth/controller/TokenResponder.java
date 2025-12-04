package com.example.ktb3community.auth.controller;

import com.example.ktb3community.auth.dto.AuthResponse;
import com.example.ktb3community.auth.dto.TokenDto;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TokenResponder {

    public ResponseEntity<ApiResult<AuthResponse>> success(
            TokenDto tokenDto,
            HttpServletResponse response,
            HttpStatus status
    ) {
        CookieUtil.addRefreshTokenCookie(response, tokenDto.refreshToken());
        AuthResponse authResponse = new AuthResponse(tokenDto.accessToken());

        return ResponseEntity
                .status(status)
                .body(ApiResult.ok(authResponse));
    }
}

