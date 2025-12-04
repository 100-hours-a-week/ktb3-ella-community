package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.dto.TokenDto;
import com.example.ktb3community.auth.repository.RefreshTokenRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final long GRACE_PERIOD_SECONDS = 10;

    // 최초 로그인 시 새로운 familyId 생성 후 토큰 발급
    @Transactional
    public TokenDto createToken(Long userId) {
        String familyId = UUID.randomUUID().toString();
        return createRefreshToken(userId, familyId);
    }

    // 로그인 유지 시 기존 familyId로 토큰 발급
    @Transactional
    public TokenDto createRefreshToken(Long userId, String familyId) {
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, familyId);
        String newRefreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.save(RefreshToken.builder()
                .token(newRefreshToken)
                .userId(userId)
                .familyId(familyId)
                .build());

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public TokenDto rotate(String oldRefreshToken, String oldAccessToken) {
        // 동시 요청인지 먼저 확인
        String graceKey = "grace:" + oldRefreshToken;
        String cachedTokenJson = redisTemplate.opsForValue().get(graceKey);

        if(cachedTokenJson != null) {
            try {
                return objectMapper.readValue(cachedTokenJson, TokenDto.class);
            } catch (JsonProcessingException e){
                log.error("Grace Period 캐시 파싱 실패");
            }
        }

        String familyId = jwtTokenProvider.getClaim(oldAccessToken, "familyId");
        Long userId = Long.valueOf(jwtTokenProvider.getClaim(oldAccessToken, "userId"));

        RefreshToken oldToken = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElse(null);

        if(oldToken == null ) {
            List<RefreshToken> familyTokens = refreshTokenRepository.findAllByFamilyId(familyId);
            if (!familyTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(familyTokens);
            }
            throw new BusinessException(ErrorCode.INVALID_TOKEN_REUSE_DETECTED);
        }

        refreshTokenRepository.delete(oldToken);
        TokenDto newTokenDto = createRefreshToken(userId, familyId);

        try {
            String newTokenJson = objectMapper.writeValueAsString(newTokenDto);
            redisTemplate.opsForValue().set(
                    graceKey,
                    newTokenJson,
                    Duration.ofSeconds(GRACE_PERIOD_SECONDS)
            );
        } catch (JsonProcessingException e) {
            log.error("Grace Period 캐시 저장 실패");
        }

        return newTokenDto;
    }

    @Transactional
    public void revoke(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void revokeAllByUser(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
        if (!tokens.isEmpty()) {
            refreshTokenRepository.deleteAll(tokens);
        }
    }
}
