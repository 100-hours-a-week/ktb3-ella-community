package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class AuthService {
    private final InMemoryUserRepository inMemoryUserRepository;

    public MeResponse signup(SignUpRequest signUpRequest) {
        String email = signUpRequest.email().trim().toLowerCase();
        if (inMemoryUserRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
        if (inMemoryUserRepository.existsByNickname(signUpRequest.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST);
        }
        User saved = inMemoryUserRepository.save(User.createNew(email, signUpRequest.password(), signUpRequest.nickname(), signUpRequest.profileImageUrl(), Instant.now()));
        return new MeResponse(saved.getEmail(), saved.getNickname(), saved.getProfileImageUrl());
    }
}
