package com.example.ktb3community.auth.domain;

import com.example.ktb3community.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Instant expiresAt;

    private boolean revoked;

    private RefreshToken(Long id, String token, User user, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static RefreshToken createNew(String token, User user, Instant expiresAt) {
        return new RefreshToken(null, token, user, expiresAt, false);
    }

    public void revoke() {
        this.revoked = true;
    }
}
