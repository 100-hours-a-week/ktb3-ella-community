package com.example.ktb3community.user.domain;

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

    public void updateProfile(String nickname, String profileImageUrl, Instant now) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = now;
    }

    public void delete(Instant now) {
        if (this.deletedAt == null) {
            this.deletedAt = now;
            this.updatedAt = now;
        }
    }
}
