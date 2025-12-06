package com.example.ktb3community.auth;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.dto.TokenDto;
import com.example.ktb3community.auth.repository.RefreshTokenRepository;
import com.example.ktb3community.auth.service.RefreshTokenService;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.example.ktb3community.TestEntityFactory.user;
import static com.example.ktb3community.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock UserRepository userRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock ObjectMapper objectMapper;

    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("createRefreshToken: 유저 조회 후 AT/RT를 생성하고 RT를 저장한다")
    void createRefreshToken_success() {
        User user = user().id(USER_ID).build();

        given(jwtTokenProvider.createAccessToken(user, FAMILY_ID)).willReturn(ACCESS_TOKEN);

        ArgumentCaptor<RefreshToken> rtCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        TokenDto result = refreshTokenService.createRefreshToken(user, FAMILY_ID);

        verify(refreshTokenRepository).save(rtCaptor.capture());
        RefreshToken saved = rtCaptor.getValue();

        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getFamilyId()).isEqualTo(FAMILY_ID);
        assertThat(saved.getToken()).isNotNull();

        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(saved.getToken());
    }

    @Test
    @DisplayName("createToken: 새로운 familyId를 생성하고 AT/RT에 동일하게 사용한다")
    void createToken_success() {
        User user = user().id(USER_ID).build();

        given(jwtTokenProvider.createAccessToken(eq(user), anyString()))
                .willReturn(ACCESS_TOKEN);

        ArgumentCaptor<String> familyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RefreshToken> rtCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        TokenDto result = refreshTokenService.createToken(user);

        verify(jwtTokenProvider).createAccessToken(eq(user), familyCaptor.capture());
        String usedFamilyId = familyCaptor.getValue();

        verify(refreshTokenRepository).save(rtCaptor.capture());
        RefreshToken saved = rtCaptor.getValue();

        assertThat(usedFamilyId).isNotNull();
        assertThat(saved.getFamilyId()).isEqualTo(usedFamilyId);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);

        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(saved.getToken());
    }

    @Test
    @DisplayName("rotate: Grace 캐시에 값이 있으면 캐시 결과를 그대로 반환한다")
    void rotate_graceCache_hit() throws Exception {
        TokenDto cached = new TokenDto("cached-at", "cached-rt");
        stubGraceCacheHit(OLD_RT, cached);

        TokenDto result = refreshTokenService.rotate(OLD_RT, OLD_AT);

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(refreshTokenRepository, jwtTokenProvider, userRepository);
    }

    @Test
    @DisplayName("rotate: 정상적인 RT이면 기존 RT를 삭제하고 새로운 토큰 쌍을 발급한다")
    void rotate_success() throws Exception {
        stubGraceCacheMiss(OLD_RT);
        stubValidClaims(OLD_AT, FAMILY_ID, USER_ID);

        RefreshToken oldToken = rt(OLD_RT, USER_ID, FAMILY_ID);
        given(refreshTokenRepository.findByToken(OLD_RT)).willReturn(Optional.of(oldToken));

        User user = user().id(USER_ID).build();
        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(jwtTokenProvider.createAccessToken(user, FAMILY_ID)).willReturn("new-access");

        given(objectMapper.writeValueAsString(any(TokenDto.class))).willReturn("json");

        ArgumentCaptor<RefreshToken> newRtCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        TokenDto result = refreshTokenService.rotate(OLD_RT, OLD_AT);

        verify(refreshTokenRepository).delete(oldToken);
        verify(refreshTokenRepository).save(newRtCaptor.capture());

        RefreshToken saved = newRtCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getFamilyId()).isEqualTo(FAMILY_ID);
        assertThat(saved.getToken()).isNotNull();

        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo(saved.getToken());

        verify(valueOperations).set(eq(graceKey(OLD_RT)), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("rotate: accessToken에서 familyId가 null이면 INVALID_ACCESS_TOKEN 예외")
    void rotate_invalidAccessToken_familyIdNull() {
        stubGraceCacheMiss(OLD_RT);
        given(jwtTokenProvider.getFamilyIdFromAccessToken(OLD_AT)).willReturn(null);

        Throwable thrown = catchThrowable(() -> refreshTokenService.rotate(OLD_RT, OLD_AT));

        assertBusinessError(thrown, ErrorCode.INVALID_ACCESS_TOKEN);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("rotate: RT가 DB에 없고 같은 familyId의 다른 토큰이 있으면 재사용 공격으로 간주하고 모두 삭제한다")
    void rotate_oldTokenNotFound_reuseDetected() {
        stubGraceCacheMiss(OLD_RT);
        stubValidClaims(OLD_AT, FAMILY_ID, USER_ID);

        given(refreshTokenRepository.findByToken(OLD_RT)).willReturn(Optional.empty());

        List<RefreshToken> familyTokens = List.of(
                rt("other-rt", USER_ID, FAMILY_ID)
        );
        given(refreshTokenRepository.findAllByFamilyId(FAMILY_ID)).willReturn(familyTokens);

        Throwable thrown = catchThrowable(() -> refreshTokenService.rotate(OLD_RT, OLD_AT));

        assertBusinessError(thrown, ErrorCode.INVALID_TOKEN_REUSE_DETECTED);
        verify(refreshTokenRepository).deleteAll(familyTokens);
    }

    @Test
    @DisplayName("rotate: familyId 또는 userId가 일치하지 않으면 INVALID_REFRESH_TOKEN 발생 및 family 전체 삭제")
    void rotate_familyOrUserMismatch_invalidRefreshToken() {
        stubGraceCacheMiss(OLD_RT);
        stubValidClaims(OLD_AT, "claims-family", USER_ID);

        RefreshToken oldToken = rt(OLD_RT, USER_ID, "db-family");
        given(refreshTokenRepository.findByToken(OLD_RT)).willReturn(Optional.of(oldToken));

        List<RefreshToken> familyTokens = List.of(
                rt("rt1", USER_ID, oldToken.getFamilyId())
        );
        given(refreshTokenRepository.findAllByFamilyId(oldToken.getFamilyId()))
                .willReturn(familyTokens);

        Throwable thrown = catchThrowable(() -> refreshTokenService.rotate(OLD_RT, OLD_AT));

        assertBusinessError(thrown, ErrorCode.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository).deleteAll(familyTokens);
        verify(refreshTokenRepository).delete(oldToken);
    }

    @Test
    @DisplayName("rotate: Grace 캐시 JSON 파싱 실패 시 캐시를 무시하고 정상 로테이션을 진행한다")
    void rotate_graceCache_parseError_fallbackToNormalFlow() throws Exception {
        String key = graceKey(OLD_RT);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn("broken-json");
        given(objectMapper.readValue("broken-json", TokenDto.class))
                .willThrow(new JsonProcessingException("parse error") {});

        stubValidClaims(OLD_AT, FAMILY_ID, USER_ID);

        RefreshToken oldToken = rt(OLD_RT, USER_ID, FAMILY_ID);
        given(refreshTokenRepository.findByToken(OLD_RT)).willReturn(Optional.of(oldToken));

        User user = user().id(USER_ID).build();
        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(jwtTokenProvider.createAccessToken(user, FAMILY_ID)).willReturn("new-access");

        given(objectMapper.writeValueAsString(any(TokenDto.class))).willReturn("json");

        TokenDto result = refreshTokenService.rotate(OLD_RT, OLD_AT);

        assertThat(result.accessToken()).isEqualTo("new-access");
        verify(refreshTokenRepository).delete(oldToken);
    }

    @Test
    @DisplayName("rotate: Grace 캐시 저장 시 직렬화 실패해도 예외를 던지지 않는다")
    void rotate_graceCache_serializeError() throws Exception {
        stubGraceCacheMiss(OLD_RT);
        stubValidClaims(OLD_AT, FAMILY_ID, USER_ID);

        RefreshToken oldToken = rt(OLD_RT, USER_ID, FAMILY_ID);
        given(refreshTokenRepository.findByToken(OLD_RT)).willReturn(Optional.of(oldToken));

        User user = user().id(USER_ID).build();
        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(jwtTokenProvider.createAccessToken(user, FAMILY_ID)).willReturn("new-access");

        given(objectMapper.writeValueAsString(any(TokenDto.class)))
                .willThrow(new JsonProcessingException("serialize error") {});

        TokenDto result = refreshTokenService.rotate(OLD_RT, OLD_AT);

        assertThat(result.accessToken()).isEqualTo("new-access");

        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("rotate: RT도 없고 동일 familyId 토큰도 없으면 재사용 공격으로 간주하지만 deleteAll은 호출하지 않는다")
    void rotate_oldTokenNotFound_noFamilyTokens() {
        stubGraceCacheMiss(OLD_RT);
        stubValidClaims(OLD_AT, FAMILY_ID, USER_ID);

        given(refreshTokenRepository.findByToken(OLD_RT)).willReturn(Optional.empty());
        given(refreshTokenRepository.findAllByFamilyId(FAMILY_ID)).willReturn(List.of());

        Throwable thrown = catchThrowable(() -> refreshTokenService.rotate(OLD_RT, OLD_AT));

        assertBusinessError(thrown, ErrorCode.INVALID_TOKEN_REUSE_DETECTED);

        verify(refreshTokenRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("rotate: familyId 또는 userId 불일치이고 family 토큰이 없으면 oldToken만 삭제한다")
    void rotate_familyOrUserMismatch_noFamilyTokens() {
        stubGraceCacheMiss(OLD_RT);
        stubValidClaims(OLD_AT, "claims-family", USER_ID);

        RefreshToken oldToken = rt(OLD_RT, USER_ID, "db-family");
        given(refreshTokenRepository.findByToken(OLD_RT)).willReturn(Optional.of(oldToken));

        given(refreshTokenRepository.findAllByFamilyId(oldToken.getFamilyId()))
                .willReturn(List.of());

        Throwable thrown = catchThrowable(() -> refreshTokenService.rotate(OLD_RT, OLD_AT));

        assertBusinessError(thrown, ErrorCode.INVALID_REFRESH_TOKEN);

        verify(refreshTokenRepository, never()).deleteAll(anyList());
        verify(refreshTokenRepository).delete(oldToken);
    }

    @Test
    @DisplayName("revoke: 해당 RT가 존재하면 삭제한다")
    void revoke_success() {
        String rtValue = "rt-value";
        RefreshToken token = rt(rtValue, USER_ID, FAMILY_ID);

        given(refreshTokenRepository.findByToken(rtValue)).willReturn(Optional.of(token));

        refreshTokenService.revoke(rtValue);

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    @DisplayName("revoke: RT가 존재하지 않으면 아무 작업도 하지 않는다")
    void revoke_notFound_noOp() {
        String rtValue = "rt-value";
        given(refreshTokenRepository.findByToken(rtValue)).willReturn(Optional.empty());

        refreshTokenService.revoke(rtValue);

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("revokeAllByUser: 해당 유저의 RT가 있으면 모두 삭제한다")
    void revokeAllByUser_success() {
        List<RefreshToken> tokens = List.of(
                rt("rt1", USER_ID, "f1"),
                rt("rt2", USER_ID, "f2")
        );
        given(refreshTokenRepository.findAllByUserId(USER_ID)).willReturn(tokens);

        refreshTokenService.revokeAllByUser(USER_ID);

        verify(refreshTokenRepository).deleteAll(tokens);
    }

    @Test
    @DisplayName("revokeAllByUser: 해당 유저의 RT가 없으면 deleteAll을 호출하지 않는다")
    void revokeAllByUser_noTokens() {
        given(refreshTokenRepository.findAllByUserId(USER_ID)).willReturn(List.of());

        refreshTokenService.revokeAllByUser(USER_ID);

        verify(refreshTokenRepository, never()).deleteAll(anyList());
    }

    private RefreshToken rt(String token, Long userId, String familyId) {
        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .familyId(familyId)
                .build();
    }

    private String graceKey(String oldRefreshToken) {
        return "grace:" + oldRefreshToken;
    }

    private void stubGraceCacheHit(String oldRefreshToken, TokenDto dto) throws JsonProcessingException {
        String key = graceKey(oldRefreshToken);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn("cached-json");
        given(objectMapper.readValue("cached-json", TokenDto.class)).willReturn(dto);
    }

    private void stubGraceCacheMiss(String oldRefreshToken) {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(graceKey(oldRefreshToken))).willReturn(null);
    }

    private void stubValidClaims(String accessToken, String familyId, Long userId) {
        given(jwtTokenProvider.getFamilyIdFromAccessToken(accessToken)).willReturn(familyId);
        given(jwtTokenProvider.getUserIdFromAccessToken(accessToken)).willReturn(userId);
    }

    private void assertBusinessError(Throwable thrown, ErrorCode expected) {
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }
}
