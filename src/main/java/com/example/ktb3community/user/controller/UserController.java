package com.example.ktb3community.user.controller;

import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.common.doc.ApiCommonErrorResponses;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.response.ApiResult;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.dto.UpdatePasswordRequest;
import com.example.ktb3community.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "이메일, 닉네임 중복 검증", description = "이메일 또는 닉네임 중 하나라도 있으면 중복 검증을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "필수 요청 파라미터가 누락되었습니다.")
    })
    @ApiCommonErrorResponses
    @GetMapping("/availability")
    public ResponseEntity<ApiResult<AvailabilityResponse>> getAvailability(
            @Parameter(description = "이메일", example = "lydbsdud@gmail.com") @RequestParam(required = false) String email,
            @Parameter(description = "닉네임", example = "ella")@RequestParam(required = false) String nickname) {
        if(email == null && nickname == null){
            throw new BusinessException(ErrorCode.MISSING_PARAMETER);
        }
        AvailabilityResponse availabilityResponse = userService.getAvailability(email, nickname);
        return ResponseEntity.ok(ApiResult.ok(availabilityResponse));
    }

    @Operation(summary = "내 정보 조회", description = "사용자 id로 내 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @GetMapping("/me")
    public ResponseEntity<ApiResult<MeResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getId();
        MeResponse me = userService.getMe(userId);
        return ResponseEntity.ok(ApiResult.ok(me));
    }

    @Operation(summary = "내 정보 수정", description = "사용자 id로 내 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @PatchMapping("me")
    public ResponseEntity<ApiResult<MeResponse>> updateMe(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UpdateMeRequest updateMeRequest) {
        Long userId = customUserDetails.getId();
        MeResponse me = userService.updateMe(userId, updateMeRequest);
        return ResponseEntity.ok(ApiResult.ok(me));
    }

    @Operation(summary = "비밀번호 수정", description = "사용자 id로 비밀번호를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @PostMapping("me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        Long userId = customUserDetails.getId();
        userService.updatePassword(userId, updatePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 id로 회원 탈퇴를 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @ApiCommonErrorResponses
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawMe(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            HttpServletResponse response) {
        Long userId = customUserDetails.getId();
        userService.withdrawMe(userId, response);
        return ResponseEntity.noContent().build();
    }
}
