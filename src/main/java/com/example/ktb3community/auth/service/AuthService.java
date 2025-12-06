package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.dto.TokenDto;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenDto signup(SignUpRequest signUpRequest) {
        String email = signUpRequest.email().trim().toLowerCase();
        String hashedPassword = passwordEncoder.encode(signUpRequest.password());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
        if (userRepository.existsByNickname(signUpRequest.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST);
        }
        User saved = userRepository.save(User.createNew(email, hashedPassword, signUpRequest.nickname(), signUpRequest.profileImageUrl(), Role.ROLE_USER));

        return refreshTokenService.createToken(saved);
    }

    @Transactional
    public TokenDto login(LoginRequest loginRequest) {
        String email = loginRequest.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new UserNotFoundException();
        }
        return refreshTokenService.createToken(user);
    }

    @Transactional
    public TokenDto refresh(String oldRefreshToken, String oldAccessToken) {
        return refreshTokenService.rotate(oldRefreshToken, oldAccessToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}
