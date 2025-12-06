package com.example.ktb3community.auth;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.example.ktb3community.TestFixtures.TOKEN_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    @DisplayName("createNew: 토큰 생성 시 version은 0L로 초기화되고, revoked는 false다")
    void createNew_success() {
        User user = user().id(USER_ID).build();
        Instant expiresAt = Instant.now().plusSeconds(3600);

        RefreshToken token = RefreshToken.createNew(TOKEN_ID, user, expiresAt);

        assertThat(token.getId()).isEqualTo(TOKEN_ID);
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getExpiresAt()).isEqualTo(expiresAt);

        assertThat(token.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("revoke: 토큰을 폐기하면 revoked 상태가 true가 된다")
    void revoke_success() {
        RefreshToken token = RefreshToken.createNew(TOKEN_ID, user().build(), Instant.now());
        assertThat(token.isRevoked()).isFalse();

        token.revoke();

        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("revoke: 이미 폐기된 토큰을 다시 폐기해도 상태는 유지된다")
    void revoke_idempotent() {
        RefreshToken token = RefreshToken.createNew(TOKEN_ID, user().build(), Instant.now());
        token.revoke();

        token.revoke();

        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("isExpired: 만료 시간이 현재 시간보다 이전이면 true를 반환한다")
    void isExpired_true() {
        Instant now = Instant.now();
        Instant past = now.minusSeconds(10);

        RefreshToken token = RefreshToken.builder()
                .expiresAt(past)
                .build();

        assertThat(token.isExpired(now)).isTrue();
    }

    @Test
    @DisplayName("isExpired: 만료 시간이 현재 시간보다 이후이면 false를 반환한다")
    void isExpired_false() {
        Instant now = Instant.now();
        Instant future = now.plusSeconds(10);

        RefreshToken token = RefreshToken.builder()
                .expiresAt(future)
                .build();

        assertThat(token.isExpired(now)).isFalse();
    }
}
