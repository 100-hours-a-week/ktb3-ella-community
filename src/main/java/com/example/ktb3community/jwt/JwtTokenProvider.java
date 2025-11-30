package com.example.ktb3community.jwt;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-exp-minutes}")
    private long accessExpMinutes;

    @Value("${app.jwt.refresh-exp-days}")
    private long refreshExpDays;

    // Key 객체
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessExpMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long refreshTokenId, Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(refreshExpDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .setId(refreshTokenId.toString())
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 서명 검증 + 파싱
    private Claims parseClaims(String token, ErrorCode errorCode) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new BusinessException(errorCode);
        }
    }

    public Instant getRefreshExpiresAt() {
        return Instant.now().plus(refreshExpDays, ChronoUnit.DAYS);
    }

    public String getRefreshTokenId(String refreshToken) {
        Claims claims = parseClaims(refreshToken, ErrorCode.INVALID_REFRESH_TOKEN);
        return claims.getId();
    }

    public Long getUserIdFromAccessToken(String accessToken) {
        Claims claims = parseClaims(accessToken, ErrorCode.INVALID_ACCESS_TOKEN);
        return Long.parseLong(claims.getSubject());
    }
}
