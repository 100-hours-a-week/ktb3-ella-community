package com.example.ktb3community.user.service;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.dto.UpdatePasswordRequest;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class UserService {
    private final InMemoryUserRepository inMemoryUserRepository;

    public AvailabilityResponse getAvailability(String email, String nickname) {
        Boolean emailAvailable = null;
        Boolean nicknameAvailable = null;
        if(email != null && !email.isBlank()){
            email = email.trim().toLowerCase();
            emailAvailable = !inMemoryUserRepository.existsByEmail(email);
        }
        if(nickname != null && !nickname.isBlank()){
            nickname = nickname.trim();
            nicknameAvailable = !inMemoryUserRepository.existsByNickname(nickname);
        }
        return new AvailabilityResponse(emailAvailable, nicknameAvailable);
    }

    public MeResponse getMe(Long userId){
        User user = inMemoryUserRepository.findByIdOrThrow(userId);
        return new MeResponse(user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }

    public MeResponse updateMe(Long userId, UpdateMeRequest updateMeRequest){
        User user = inMemoryUserRepository.findByIdOrThrow(userId);
        String nickname = updateMeRequest.nickname();
        if (nickname != null && !nickname.isBlank() && !nickname.equals(user.getNickname())) {
            inMemoryUserRepository.findByNickname(nickname)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> { throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST); });
            user.updateNickname(nickname, Instant.now());
        }
        if(updateMeRequest.profileImageUrl() != null && !updateMeRequest.profileImageUrl().isBlank()){
            user.updateProfileImageUrl(updateMeRequest.profileImageUrl(), Instant.now());
        }
        inMemoryUserRepository.save(user);
        return new MeResponse(user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }

    public void updatePassword(Long userId, UpdatePasswordRequest updatePasswordRequest){
        User user = inMemoryUserRepository.findByIdOrThrow(userId);
        user.updatePasswordHash(updatePasswordRequest.newPassword(), Instant.now());
        inMemoryUserRepository.save(user);
    }

    public void withdrawMe(Long userId){
        User user = inMemoryUserRepository.findByIdOrThrow(userId);
        //TODO: 쿠키 즉시 만료 로직 추가
        user.delete(Instant.now());
        inMemoryUserRepository.save(user);
    }
}