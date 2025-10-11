package com.example.ktb3community.auth.controller;

import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.service.AuthService;
import com.example.ktb3community.common.response.ApiResponse;
import com.example.ktb3community.user.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MeResponse>> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        MeResponse me = authService.signup(signUpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(me));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MeResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        MeResponse me = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok(me));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        //TODO 쿠키 즉시 만료 로직 추가
        return ResponseEntity.noContent().build();
    }
}
