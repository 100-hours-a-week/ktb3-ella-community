package com.example.ktb3community.user.domain;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import lombok.Getter;

import java.time.Instant;

@Getter
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String nickname;
    private String profileImageUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private User(Long id, String email, String passwordHash,
                 String nickname, String profileImageUrl,
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
    // 인메모리용 임시 생성자
    public static User rehydrate(Long id, String email, String passwordHash,
                                 String nickname, String profileImageUrl,
                                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new User(id, email, passwordHash, nickname, profileImageUrl, createdAt, updatedAt, deletedAt);
    }

    public void updateNickname(String nickname, Instant now) {
        String n = nickname.trim();
        if (n.isBlank() || n.length() > 10 || n.chars().anyMatch(Character::isWhitespace)) {
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
}
