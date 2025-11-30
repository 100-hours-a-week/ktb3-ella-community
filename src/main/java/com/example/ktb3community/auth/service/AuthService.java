package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.dto.Token;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Token signup(SignUpRequest signUpRequest) {
        String email = signUpRequest.email().trim().toLowerCase();
        String hashedPassword = passwordEncoder.encode(signUpRequest.password());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
        if (userRepository.existsByNickname(signUpRequest.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST);
        }
        User saved = userRepository.save(User.createNew(email, hashedPassword, signUpRequest.nickname(), signUpRequest.profileImageUrl(), Role.ROLE_USER));

        return issueTokens(saved);
    }

    @Transactional
    public Token login(LoginRequest loginRequest) {
        String email = loginRequest.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new UserNotFoundException();
        }
        return issueTokens(user);
    }

    @Transactional
    public Token refresh(String oldRefreshToken) {
        // JWT 검증 + revoked/expired 검사
        RefreshToken refreshToken = refreshTokenService.getValidTokenOrThrow(oldRefreshToken);
        User user = refreshToken.getUser();

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String newRefreshToken = refreshTokenService.rotate(oldRefreshToken);

        return new Token(accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private Token issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new Token(accessToken, refreshToken);
    }
}
