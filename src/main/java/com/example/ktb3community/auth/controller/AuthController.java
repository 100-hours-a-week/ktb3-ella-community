package com.example.ktb3community.auth.controller;

import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.service.AuthService;
import com.example.ktb3community.common.doc.ApiCommonErrorResponses;
import com.example.ktb3community.user.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.example.ktb3community.common.response.ApiResult;
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

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "OK"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다.")

    })
    @ApiCommonErrorResponses
    @PostMapping("/signup")
    public ResponseEntity<ApiResult<MeResponse>> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        MeResponse me = authService.signup(signUpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.ok(me));
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @PostMapping("/login")
    public ResponseEntity<ApiResult<MeResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        MeResponse me = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResult.ok(me));
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    @ApiCommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        //TODO 쿠키 즉시 만료 로직 추가
        return ResponseEntity.noContent().build();
    }
}
