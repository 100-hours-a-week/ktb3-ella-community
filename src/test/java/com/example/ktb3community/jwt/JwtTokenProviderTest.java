package com.example.ktb3community.jwt;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "test_secret_key_must_be_over_32_bytes_long_1234";
    private static final long ACCESS_EXP_MIN = 30;
    private static final long REFRESH_EXP_DAYS = 7;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessExpMinutes", ACCESS_EXP_MIN);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpDays", REFRESH_EXP_DAYS);
    }

    @Test
    @DisplayName("createAccessToken: 유저 ID로 유효한 JWT 액세스 토큰을 생성하고, 파싱하여 ID를 복구할 수 있다")
    void createAccessToken_success() {
        Long userId = 100L;

        String token = jwtTokenProvider.createAccessToken(userId);

        assertThat(token).isNotNull();
        Long parsedUserId = jwtTokenProvider.getUserIdFromAccessToken(token);
        assertThat(parsedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("createRefreshToken: 토큰 ID와 유저 ID로 리프레시 토큰을 생성하고, 파싱하여 토큰 ID를 복구할 수 있다")
    void createRefreshToken_success() {
        Long refreshTokenId = 555L;
        Long userId = 100L;

        String token = jwtTokenProvider.createRefreshToken(refreshTokenId, userId);

        assertThat(token).isNotNull();
        Long parsedTokenId = jwtTokenProvider.getRefreshTokenId(token);
        assertThat(parsedTokenId).isEqualTo(refreshTokenId);
    }

    @Test
    @DisplayName("getUserIdFromAccessToken: 잘못된 토큰(서명 불일치 등) 입력 시 INVALID_ACCESS_TOKEN 예외 발생")
    void getUserIdFromAccessToken_invalid_throws() {
        String invalidToken = "invalid.token.structure";

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromAccessToken(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("getUserIdFromAccessToken: 다른 시크릿 키로 서명된 토큰은 파싱에 실패한다")
    void getUserIdFromAccessToken_wrongSignature_throws() {
        String otherSecret = "other_secret_key_must_be_over_32_bytes_long_9999";
        String fakeToken = Jwts.builder()
                .setSubject("100")
                .signWith(Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromAccessToken(fakeToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("getRefreshTokenId: 잘못된 토큰 입력 시 INVALID_REFRESH_TOKEN 예외 발생")
    void getRefreshTokenId_invalid_throws() {
        String invalidToken = "invalid.token";

        assertThatThrownBy(() -> jwtTokenProvider.getRefreshTokenId(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("getRefreshExpiresAt: 리프레시 토큰 만료일 계산 로직이 설정값(7일)과 근사한지 확인")
    void getRefreshExpiresAt_check() {
        Instant now = Instant.now();

        Instant expiresAt = jwtTokenProvider.getRefreshExpiresAt();

        Instant expected = now.plus(REFRESH_EXP_DAYS, ChronoUnit.DAYS);
        long diffSeconds = Math.abs(expected.getEpochSecond() - expiresAt.getEpochSecond());

        assertThat(diffSeconds).isLessThan(60);
    }

    @Test
    @DisplayName("만료된 토큰 파싱 시 예외가 발생한다")
    void parse_expired_token_throws() {
        Date past = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        String expiredToken = Jwts.builder()
                .setSubject("100")
                .setExpiration(past)
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromAccessToken(expiredToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
    }
}
