package com.example.ktb3community.user.domain;

import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.constants.ValidationConstant;
import com.example.ktb3community.common.domain.BaseTimeEntity;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

    public static User createNew(String email, String passwordHash,
                                 String nickname, String profileImageUrl, Role role) {
        return User.builder()
                .email(email.toLowerCase().trim())
                .passwordHash(passwordHash)
                .nickname(nickname.trim())
                .profileImageUrl(profileImageUrl.trim())
                .role(role)
                .build();
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
