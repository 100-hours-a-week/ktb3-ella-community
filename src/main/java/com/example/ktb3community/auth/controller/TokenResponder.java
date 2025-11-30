package com.example.ktb3community.auth.controller;

import com.example.ktb3community.auth.dto.AuthResponse;
import com.example.ktb3community.auth.dto.Token;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TokenResponder {

    public ResponseEntity<ApiResult<AuthResponse>> success(
            Token token,
            HttpServletResponse response,
            HttpStatus status
    ) {
        CookieUtil.addRefreshTokenCookie(response, token.refreshToken());
        AuthResponse authResponse = new AuthResponse(token.accessToken());

        return ResponseEntity
                .status(status)
                .body(ApiResult.ok(authResponse));
    }
}

