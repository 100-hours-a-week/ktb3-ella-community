package com.example.ktb3community.user.domain;

import com.example.ktb3community.common.constants.ValidationConstant;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "profile_image_url" ,nullable = false)
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private User(Long id, String email, String passwordHash, String nickname, String profileImageUrl,
                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.nickname = nickname.trim();
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static User createNew(String email, String password,
                                 String nickname, String profileImageUrl, Instant now) {
        // TODO: Spring Security 추가 시 비밀번호 암호화 추가
        return new User(null, email, password, nickname, profileImageUrl, now, now, null);
    }

    public static User rehydrate(Long id, String email, String passwordHash,
                                 String nickname, String profileImageUrl,
                                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new User(id, email, passwordHash, nickname, profileImageUrl, createdAt, updatedAt, deletedAt);
    }

    public void updateNickname(String nickname, Instant now) {
        String n = nickname.trim();
        if (n.isBlank() || n.length() > ValidationConstant.NICKNAME_MAX_LENGTH || n.chars().anyMatch(Character::isWhitespace)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!n.equals(this.nickname)) { this.nickname = n; this.updatedAt = now; }
    }

    public void updateProfileImageUrl(String profileImageUrl, Instant now) {
        String p = profileImageUrl.trim();
        if (p.isBlank()) { throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); }
        if (!p.equals(this.profileImageUrl)) { this.profileImageUrl = p; this.updatedAt = now; }
    }

    public void delete(Instant now) {
        if (this.deletedAt == null) {
            this.deletedAt = now;
            this.updatedAt = now;
        }
    }

    public void updatePasswordHash(String passwordHash, Instant now) {
        this.passwordHash = passwordHash;
        this.updatedAt = now;
    }
}
