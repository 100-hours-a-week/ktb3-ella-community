package com.example.ktb3community.auth.service;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.dto.TokenDto;
import com.example.ktb3community.auth.repository.RefreshTokenRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.UserRepository;
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
    private final UserRepository userRepository;
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
        User user = userRepository.findByIdOrThrow(userId);
        String newAccessToken = jwtTokenProvider.createAccessToken(user, familyId);
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
        // grace 기간 내 동시 요청 확인
        TokenDto cached = getFromGraceCache(oldRefreshToken);
        if (cached != null) {
            return cached;
        }
        AccessClaims claims = extractClaimsOrThrow(oldAccessToken);
        RefreshToken oldToken = validateAndHandleOldToken(oldRefreshToken, claims);

        refreshTokenRepository.delete(oldToken);
        TokenDto newTokenDto = createRefreshToken(oldToken.getUserId(), oldToken.getFamilyId());
        putToGraceCache(oldRefreshToken, newTokenDto);

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

    // Grace Period 캐시에서 토큰 조회
    private TokenDto getFromGraceCache(String oldRefreshToken) {
        String key = "grace:" + oldRefreshToken;
        String cachedJson = redisTemplate.opsForValue().get(key);
        if (cachedJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cachedJson, TokenDto.class);
        } catch (JsonProcessingException e) {
            log.error("Grace Period 캐시 파싱 실패", e);
            return null;
        }
    }

    // Grace Period 캐시에 토큰 저장
    private void putToGraceCache(String oldRefreshToken, TokenDto tokenDto) {
        try {
            String key = "grace:" + oldRefreshToken;
            String json = objectMapper.writeValueAsString(tokenDto);
            redisTemplate.opsForValue().set(
                    key,
                    json,
                    Duration.ofSeconds(GRACE_PERIOD_SECONDS)
            );
        } catch (JsonProcessingException e) {
            log.error("Grace Period 캐시 저장 실패", e);
        }
    }

    // AccessToken에서 familyId, userId 추출 및 검증
    private AccessClaims extractClaimsOrThrow(String accessToken) {
        String familyId = jwtTokenProvider.getFamilyIdFromAccessToken(accessToken);
        String userIdClaim = jwtTokenProvider.getUserIdFromAccessToken(accessToken).toString();

        if (familyId == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        try {
            Long userId = Long.valueOf(userIdClaim);
            return new AccessClaims(familyId, userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    // old RT 검증 및 탈취/위조 처리
    private RefreshToken validateAndHandleOldToken(String oldRefreshToken, AccessClaims claims) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElse(null);

        // old RT 자체가 없는 경우
        if (oldToken == null) {
            List<RefreshToken> familyTokens = refreshTokenRepository.findAllByFamilyId(claims.familyId());
            if (!familyTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(familyTokens);
            }
            throw new BusinessException(ErrorCode.INVALID_TOKEN_REUSE_DETECTED);
        }

        // familyId or userId 불일치 → 탈취/위조 가능성
        boolean sameFamily = oldToken.getFamilyId().equals(claims.familyId());
        boolean sameUser = oldToken.getUserId().equals(claims.userId());

        if (!sameFamily || !sameUser) {
            List<RefreshToken> familyTokens = refreshTokenRepository.findAllByFamilyId(oldToken.getFamilyId());
            if (!familyTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(familyTokens);
            }
            refreshTokenRepository.delete(oldToken);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return oldToken;
    }

    // 접근 토큰에서 familyId, userId 추출용 레코드
    private record AccessClaims(String familyId, Long userId) {}

}
