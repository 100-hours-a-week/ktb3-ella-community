package com.example.ktb3community.user.service;

import com.example.ktb3community.comment.repository.CommentRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.dto.UpdatePasswordRequest;
import com.example.ktb3community.user.mapper.UserMapper;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(String email, String nickname) {
        Boolean emailAvailable = null;
        Boolean nicknameAvailable = null;
        if(email != null && !email.isBlank()){
            email = email.trim().toLowerCase();
            emailAvailable = !userRepository.existsByEmail(email);
        }
        if(nickname != null && !nickname.isBlank()){
            nickname = nickname.trim();
            nicknameAvailable = !userRepository.existsByNickname(nickname);
        }
        return new AvailabilityResponse(emailAvailable, nicknameAvailable);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(Long userId){
        User user = userRepository.findByIdOrThrow(userId);
        return userMapper.userToMeResponse(user);
    }

    @Transactional
    public MeResponse updateMe(Long userId, UpdateMeRequest updateMeRequest){
        User user = userRepository.findByIdOrThrow(userId);
        String nickname = updateMeRequest.nickname();
        if (nickname != null && !nickname.isBlank() && !nickname.equals(user.getNickname())) {
            userRepository.findByNickname(nickname)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> { throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST); });
            user.updateNickname(nickname, Instant.now());
        }
        if(updateMeRequest.profileImageUrl() != null && !updateMeRequest.profileImageUrl().isBlank()){
            user.updateProfileImageUrl(updateMeRequest.profileImageUrl(), Instant.now());
        }
        return userMapper.userToMeResponse(user);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest updatePasswordRequest){
        User user = userRepository.findByIdOrThrow(userId);
        user.updatePasswordHash(updatePasswordRequest.newPassword(), Instant.now());
    }

    @Transactional
    public void withdrawMe(Long userId){
        userRepository.findByIdOrThrow(userId);
        Instant now = Instant.now();
        postRepository.softDeleteByUserId(userId, now);
        commentRepository.softDeleteByUserId(userId, now);
        //TODO: 쿠키 즉시 만료 로직 추가
        userRepository.softDeleteById(userId, now);
    }
}