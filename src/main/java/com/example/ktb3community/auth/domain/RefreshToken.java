package com.example.ktb3community.auth.domain;

import com.example.ktb3community.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    private RefreshToken(String id, User user, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.user = user;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static RefreshToken createNew(User user, Instant expiresAt) {
        String id = UUID.randomUUID().toString();
        return new RefreshToken(id, user, expiresAt, false);
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }
}
