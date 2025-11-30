package com.example.ktb3community.auth.domain;

import com.example.ktb3community.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_token_user_revoked", columnList = "user_id, revoked")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RefreshToken {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Version
    @Column(nullable = false)
    private Long version;

    public static RefreshToken createNew(Long id, User user, Instant expiresAt) {
        return RefreshToken.builder()
                .id(id)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .version(0L)
                .build();
    }

    public void revoke() {
        if(this.revoked) return;
        this.revoked = true;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }
}
