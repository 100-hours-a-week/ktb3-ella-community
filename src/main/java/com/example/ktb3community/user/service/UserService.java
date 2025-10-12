package com.example.ktb3community.user.service;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.exception.UserNotFoundException;
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

    public MeResponse getMyProfile(Long userId){
        User user = inMemoryUserRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return new MeResponse(user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }

    public MeResponse updateMyProfile(Long userId, UpdateMeRequest updateMeRequest){
        User user = inMemoryUserRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        if(updateMeRequest.nickname() != null && !updateMeRequest.nickname().isBlank()){
            if(inMemoryUserRepository.existsByNickname(updateMeRequest.nickname())){
                throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST);
            }
            user.updateNickname(updateMeRequest.nickname(), Instant.now());
        }
        if(updateMeRequest.profileImageUrl() != null && !updateMeRequest.profileImageUrl().isBlank()){
            user.updateProfileImageUrl(updateMeRequest.profileImageUrl(), Instant.now());
        }
        return new MeResponse(user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }
}