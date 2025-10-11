package com.example.ktb3community.user.controller;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.response.ApiResponse;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
