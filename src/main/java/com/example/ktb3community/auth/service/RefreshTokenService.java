package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.repository.RefreshTokenRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String createRefreshToken(User user) {
        Instant expiresAt = jwtTokenProvider.getRefreshExpiresAt();
        RefreshToken refreshToken = RefreshToken.createNew(user, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return jwtTokenProvider.createRefreshToken(
                refreshToken.getId(),
                user.getId()
        );
    }

    // JWT 서명/형식 검증
    public RefreshToken getValidTokenOrThrow(String refreshToken) {
        String refreshTokenId = jwtTokenProvider.getRefreshTokenId(refreshToken);

        RefreshToken token = refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_REFRESH_TOKEN));

        if (token.isExpired(Instant.now()) || token.isRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        return token;
    }

    @Transactional
    public String rotate(String oldRefreshToken) {
        RefreshToken oldToken = getValidTokenOrThrow(oldRefreshToken);

        oldToken.revoke();

        User user = oldToken.getUser();
        return createRefreshToken(user);
    }

    @Transactional
    public void revoke(String refreshToken) {
        String refreshTokenId = jwtTokenProvider.getRefreshTokenId(refreshToken);

        RefreshToken token = refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_REFRESH_TOKEN));

        token.revoke();
        refreshTokenRepository.save(token);
    }

}
