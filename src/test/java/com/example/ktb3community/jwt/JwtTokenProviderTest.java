package com.example.ktb3community.jwt;

import com.example.ktb3community.TestFixtures;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
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

import static com.example.ktb3community.TestEntityFactory.user;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "test_secret_key_must_be_over_32_bytes_long_1234";
    private static final long ACCESS_EXP_MIN = 30L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessExpMinutes", ACCESS_EXP_MIN);
    }


    @Test
    @DisplayName("createAccessToken: 유저와 familyId로 토큰을 생성하고, 정보를 복구할 수 있다")
    void createAccessToken_success() {
        User user = user()
                .id(USER_ID)
                .email("user@test.com")
                .build();
        String familyId = "family-1234";

        String token = jwtTokenProvider.createAccessToken(user, familyId);

        assertThat(token).isNotNull();

        Long parsedUserId = jwtTokenProvider.getUserIdFromAccessToken(token);
        String parsedEmail = jwtTokenProvider.getEmailFromAccessToken(token);
        String parsedRole = jwtTokenProvider.getRoleFromAccessToken(token);
        String parsedFamilyId = jwtTokenProvider.getFamilyIdFromAccessToken(token);

        assertThat(parsedUserId).isEqualTo(USER_ID);
        assertThat(parsedEmail).isEqualTo(user.getEmail());
        assertThat(parsedRole).isEqualTo(user.getRole().name());
        assertThat(parsedFamilyId).isEqualTo(familyId);
    }

    @Test
    @DisplayName("getUserIdFromAccessToken: 잘못된 토큰(형식 등)이면 INVALID_ACCESS_TOKEN 예외 발생")
    void getUserIdFromAccessToken_invalid_throws() {
        String invalidToken = "invalid.token.structure";

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromAccessToken(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("getUserIdFromAccessToken: 다른 시크릿 키로 서명된 토큰은 INVALID_ACCESS_TOKEN 예외 발생")
    void getUserIdFromAccessToken_wrongSignature_throws() {
        String otherSecret = "other_secret_key_must_be_over_32_bytes_long_9999";
        String fakeToken = Jwts.builder()
                .setSubject(String.valueOf(TestFixtures.USER_ID))
                .signWith(
                        Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromAccessToken(fakeToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("만료된 토큰을 파싱하면 INVALID_ACCESS_TOKEN 예외가 발생한다")
    void parse_expired_token_throws() {
        Date past = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        String expiredToken = Jwts.builder()
                .setSubject(String.valueOf(TestFixtures.USER_ID))
                .setExpiration(past)
                .signWith(
                        Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromAccessToken(expiredToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
    }
}
