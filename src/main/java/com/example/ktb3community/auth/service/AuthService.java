package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.dto.AuthResponse;
import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.util.CookieUtil;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
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
    public AuthResponse signup(SignUpRequest signUpRequest, HttpServletResponse response) {
        String email = signUpRequest.email().trim().toLowerCase();
        String hashedPassword = passwordEncoder.encode(signUpRequest.password());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
        if (userRepository.existsByNickname(signUpRequest.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXIST);
        }
        User saved = userRepository.save(User.createNew(email, hashedPassword, signUpRequest.nickname(), signUpRequest.profileImageUrl(), Role.ROLE_USER));
        return issueTokens(saved, response);
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        String email = loginRequest.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new UserNotFoundException();
        }
        return issueTokens(user, response);
    }

    @Transactional
    public AuthResponse refresh(String oldRefreshToken, HttpServletResponse response) {
        // JWT 검증 + revoked/expired 검사
        RefreshToken refreshToken = refreshTokenService.getValidTokenOrThrow(oldRefreshToken);
        User user = refreshToken.getUser();

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String newRefreshToken = refreshTokenService.rotate(oldRefreshToken);

        CookieUtil.addRefreshTokenCookie(
                response,
                newRefreshToken
        );
        return new AuthResponse(accessToken);
    }

    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {

        refreshTokenService.revoke(refreshToken);

        CookieUtil.removeRefreshTokenCookie(response);
    }

    private AuthResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        CookieUtil.addRefreshTokenCookie(
                response,
                refreshToken
        );
        return new AuthResponse(accessToken);
    }

}
