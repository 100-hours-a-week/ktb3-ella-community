package com.example.ktb3community.user.domain;

import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.constants.ValidationConstant;
import com.example.ktb3community.common.domain.BaseTimeEntity;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    private Role role;


    private User(Long id, String email, String passwordHash, String nickname, String profileImageUrl, Instant deletedAt, Role role) {
        this.id = id;
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.nickname = nickname.trim();
        this.profileImageUrl = profileImageUrl;
        this.deletedAt = deletedAt;
        this.role = role;
    }

    public static User createNew(String email, String passwordHash,
                                 String nickname, String profileImageUrl, Role role) {
        return new User(null, email, passwordHash, nickname, profileImageUrl, null, role);
    }

    public static User rehydrate(Long id, String email, String passwordHash,
                                 String nickname, String profileImageUrl, Instant createdAt, Instant updatedAt, Instant deletedAt, Role role) {
        User user = new User(id, email, passwordHash, nickname, profileImageUrl, deletedAt, role);
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    public void updateNickname(String nickname) {
        String n = nickname.trim();
        if (n.isBlank() || n.length() > ValidationConstant.NICKNAME_MAX_LENGTH || n.chars().anyMatch(Character::isWhitespace)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!n.equals(this.nickname)) { this.nickname = n; }
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        String p = profileImageUrl.trim();
        if (p.isBlank()) { throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); }
        if (!p.equals(this.profileImageUrl)) { this.profileImageUrl = p; }
    }

    public void delete(Instant now) {
        if (this.deletedAt == null) {
            this.deletedAt = now;
        }
    }

    public void updatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) { throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); }
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        User other = (User) o;
        if(this.id == null || other.id == null ) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return System.identityHashCode(this);
    }
}
