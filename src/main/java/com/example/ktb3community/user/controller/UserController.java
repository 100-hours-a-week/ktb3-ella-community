package com.example.ktb3community.user.controller;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.response.ApiResponse;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.dto.UpdatePasswordRequest;
import com.example.ktb3community.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "이메일, 닉네임 중복 검증")
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(
            @RequestParam(required = false) String email, @RequestParam(required = false) String nickname) {
        if(email == null && nickname == null){
            throw new BusinessException(ErrorCode.MISSING_PARAMETER);
        }
        AvailabilityResponse availabilityResponse = userService.getAvailability(email, nickname);
        return ResponseEntity.ok(ApiResponse.ok(availabilityResponse));
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me/{userId}")
    public ResponseEntity<ApiResponse<MeResponse>> getMe(@PathVariable Long userId) {
        MeResponse me = userService.getMe(userId);
        return ResponseEntity.ok(ApiResponse.ok(me));
    }

    @Operation(summary = "내 정보 수정")
    @PatchMapping("me/{userId}")
    public ResponseEntity<ApiResponse<MeResponse>> updateMe(
            @PathVariable Long userId, @Valid @RequestBody UpdateMeRequest updateMeRequest) {
        MeResponse me = userService.updateMe(userId, updateMeRequest);
        return ResponseEntity.ok(ApiResponse.ok(me));
    }

    @Operation(summary = "비밀번호 수정")
    @PostMapping("me/password/{userId}")
    public ResponseEntity<Void> updatePassword(@PathVariable Long userId, @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        userService.updatePassword(userId, updatePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me/{userId}")
    public ResponseEntity<Void> withdrawMe(@PathVariable Long userId) {
        userService.withdrawMe(userId);
        return ResponseEntity.noContent().build();
    }

}
