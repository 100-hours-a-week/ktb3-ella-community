package com.example.ktb3community.auth;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.infra.RefreshTokenIdGenerator;
import com.example.ktb3community.auth.repository.RefreshTokenRepository;
import com.example.ktb3community.auth.service.RefreshTokenService;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static com.example.ktb3community.TestFixtures.TOKEN_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.config.Elements.JWT;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RefreshTokenIdGenerator refreshTokenIdGenerator;

    @InjectMocks
    RefreshTokenService refreshTokenService;

    private User newUser() {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    private RefreshToken newRefreshToken(Long id, User user, Instant expiresAt) {
        return RefreshToken.createNew(id, user, expiresAt);
    }

    @Test
    @DisplayName("createRefreshToken: ID 생성, DB 저장 후 JWT 문자열을 반환한다")
    void createRefreshToken_success() {
        User user = newUser();
        Instant expiresAt = Instant.now().plusSeconds(3600);

        given(jwtTokenProvider.getRefreshExpiresAt()).willReturn(expiresAt);
        given(refreshTokenIdGenerator.generate()).willReturn(TOKEN_ID);
        given(jwtTokenProvider.createRefreshToken(TOKEN_ID, USER_ID)).willReturn(JWT);

        String resultJwt = refreshTokenService.createRefreshToken(user);

        assertThat(resultJwt).isEqualTo(JWT);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();
        assertThat(savedToken.getId()).isEqualTo(TOKEN_ID);
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(savedToken.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("getValidToken: 유효한 토큰이면 객체를 반환한다")
    void getValidToken_success() {
        Long tokenId = 100L;
        RefreshToken token = newRefreshToken(tokenId, newUser(), Instant.now().plusSeconds(3600));

        given(jwtTokenProvider.getRefreshTokenId(JWT)).willReturn(tokenId);
        given(refreshTokenRepository.findById(tokenId)).willReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.getValidTokenOrThrow(JWT);

        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("getValidToken: DB에 토큰이 없으면 NOT_EXIST_REFRESH_TOKEN 예외 발생")
    void getValidToken_notFound_throws() {
        given(jwtTokenProvider.getRefreshTokenId(JWT)).willReturn(TOKEN_ID);
        given(refreshTokenRepository.findById(TOKEN_ID)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> refreshTokenService.getValidTokenOrThrow(JWT));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_EXIST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("getValidToken: 이미 만료된 토큰이면 REFRESH_TOKEN_EXPIRED 예외 발생")
    void getValidToken_expiredTime_throws() {
        RefreshToken token = newRefreshToken(TOKEN_ID, newUser(), Instant.now().minusSeconds(10));

        given(jwtTokenProvider.getRefreshTokenId(JWT)).willReturn(TOKEN_ID);
        given(refreshTokenRepository.findById(TOKEN_ID)).willReturn(Optional.of(token));

        Throwable thrown = catchThrowable(() -> refreshTokenService.getValidTokenOrThrow(JWT));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("getValidToken: 이미 폐기된 토큰이면 REFRESH_TOKEN_EXPIRED 예외 발생")
    void getValidToken_revoked_throws() {
        RefreshToken token = newRefreshToken(TOKEN_ID, newUser(), Instant.now().plusSeconds(3600));
        token.revoke();

        given(jwtTokenProvider.getRefreshTokenId(JWT)).willReturn(TOKEN_ID);
        given(refreshTokenRepository.findById(TOKEN_ID)).willReturn(Optional.of(token));

        Throwable thrown = catchThrowable(() -> refreshTokenService.getValidTokenOrThrow(JWT));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("rotate: 기존 토큰을 폐기하고 새로운 토큰을 발급한다 (Rotation)")
    void rotate_success() {
        String oldJwt = "old.jwt";
        Long oldTokenId = 100L;
        Long newTokenId = 200L;
        User user = newUser();

        RefreshToken oldToken = newRefreshToken(oldTokenId, user, Instant.now().plusSeconds(3600));

        given(jwtTokenProvider.getRefreshTokenId(oldJwt)).willReturn(oldTokenId);
        given(refreshTokenRepository.findById(oldTokenId)).willReturn(Optional.of(oldToken));

        given(refreshTokenIdGenerator.generate()).willReturn(newTokenId);
        given(jwtTokenProvider.getRefreshExpiresAt()).willReturn(Instant.now().plusSeconds(3600));
        given(jwtTokenProvider.createRefreshToken(newTokenId, 1L)).willReturn("new.jwt");

        String newJwt = refreshTokenService.rotate(oldJwt);

        assertThat(newJwt).isEqualTo("new.jwt");

        assertThat(oldToken.isRevoked()).isTrue();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken newToken = captor.getValue();
        assertThat(newToken.getId()).isEqualTo(newTokenId);
        assertThat(newToken.getUser()).isEqualTo(user);
        assertThat(newToken.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("revoke: 토큰을 명시적으로 폐기한다")
    void revoke_success() {
        RefreshToken token = newRefreshToken(TOKEN_ID, newUser(), Instant.now().plusSeconds(3600));

        given(jwtTokenProvider.getRefreshTokenId(JWT)).willReturn(TOKEN_ID);
        given(refreshTokenRepository.findById(TOKEN_ID)).willReturn(Optional.of(token));

        refreshTokenService.revoke(JWT);

        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("revoke: 존재하지 않는 토큰이면 예외가 발생한다")
    void revoke_notFound_throws() {
        given(jwtTokenProvider.getRefreshTokenId(JWT)).willReturn(TOKEN_ID);
        given(refreshTokenRepository.findById(TOKEN_ID)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> refreshTokenService.revoke(JWT));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_EXIST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("revokeAllByUser: 유저의 모든 토큰을 폐기 요청한다")
    void revokeAllByUser_success() {
        User user = newUser();

        refreshTokenService.revokeAllByUser(user);

        verify(refreshTokenRepository).revokeAllByUser(user);
    }
}