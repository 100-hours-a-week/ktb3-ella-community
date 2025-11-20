package com.example.ktb3community.user.service;

import com.example.ktb3community.auth.service.RefreshTokenService;
import com.example.ktb3community.comment.repository.CommentRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.util.CookieUtil;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.s3.service.FileService;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.dto.UpdatePasswordRequest;
import com.example.ktb3community.user.mapper.UserMapper;
import com.example.ktb3community.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

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
        String previousImageUrl = user.getProfileImageUrl();
        String nickname = updateMeRequest.nickname();
        if (nickname != null && !nickname.isBlank() && !nickname.equals(user.getNickname())) {
            userRepository.findByNickname(nickname)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> { throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST); });
            user.updateNickname(nickname);
        }
        if(updateMeRequest.profileImageUrl() != null && !updateMeRequest.profileImageUrl().isBlank()){
            user.updateProfileImageUrl(updateMeRequest.profileImageUrl());
        }
        fileService.deleteImageIfChanged(previousImageUrl, user.getProfileImageUrl());
        return userMapper.userToMeResponse(user);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest updatePasswordRequest){
        User user = userRepository.findByIdOrThrow(userId);
        String hashedPassword = passwordEncoder.encode(updatePasswordRequest.newPassword());
        user.updatePasswordHash(hashedPassword);
    }

    @Transactional
    public void withdrawMe(Long userId, HttpServletResponse response){
        User user = userRepository.findByIdOrThrow(userId);
        Instant now = Instant.now();
        postRepository.softDeleteByUserId(userId, now);
        commentRepository.softDeleteByUserId(userId, now);
        userRepository.softDeleteById(userId, now);
        refreshTokenService.revokeAllByUser(user);
        CookieUtil.removeRefreshTokenCookie(response);
    }
}