package com.example.ktb3community.user.controller;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/me/{userId}")
    public ResponseEntity<ApiResult<MeResponse>> getMe(
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId) {
        MeResponse me = userService.getMe(userId);
        return ResponseEntity.ok(ApiResult.ok(me));
    }

    @Operation(summary = "내 정보 수정", description = "사용자 id로 내 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @PatchMapping("me/{userId}")
    public ResponseEntity<ApiResult<MeResponse>> updateMe(
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId,
            @Valid @RequestBody UpdateMeRequest updateMeRequest) {
        MeResponse me = userService.updateMe(userId, updateMeRequest);
        return ResponseEntity.ok(ApiResult.ok(me));
    }

    @Operation(summary = "비밀번호 수정", description = "사용자 id로 비밀번호를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @PostMapping("me/password/{userId}")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId,
            @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        userService.updatePassword(userId, updatePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 id로 회원 탈퇴를 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.")
    })
    @DeleteMapping("/me/{userId}")
    public ResponseEntity<Void> withdrawMe(
            @Parameter(description = "사용자 id", example = "1") @PathVariable Long userId) {
        userService.withdrawMe(userId);
        return ResponseEntity.noContent().build();
    }
}
