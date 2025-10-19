package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public MeResponse signup(SignUpRequest signUpRequest) {
        String email = signUpRequest.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
        if (userRepository.existsByNickname(signUpRequest.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST);
        }
        User saved = userRepository.save(User.createNew(email, signUpRequest.password(), signUpRequest.nickname(), signUpRequest.profileImageUrl(), Instant.now()));
        return new MeResponse(saved.getEmail(), saved.getNickname(), saved.getProfileImageUrl());
    }

    public MeResponse login(LoginRequest loginRequest) {
        String email = loginRequest.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if(!user.getPasswordHash().equals(loginRequest.password())) {
            throw new UserNotFoundException();
        }
        return new MeResponse(user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }
}
