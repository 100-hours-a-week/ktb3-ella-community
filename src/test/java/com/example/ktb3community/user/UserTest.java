package com.example.ktb3community.user;

import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.constants.ValidationConstant;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User newUser() {
        return User.createNew(
                "user@test.com",
                "hash",
                "nickname",
                "http://image",
                Role.ROLE_USER
        );
    }

    @Test
    @DisplayName("createNew는 email/nickname/profile을 trim하고 email은 소문자로 변환한다")
    void createNew_normalizesFields() {
        String email = "TEST@Email.Com";
        String passwordHash = "hash";
        String nickname = "Nick";
        String profileImageUrl = "http://image";

        User user = User.createNew(email, passwordHash, nickname, profileImageUrl, Role.ROLE_USER);

        assertNull(user.getId());
        assertEquals("test@email.com", user.getEmail());
        assertEquals("Nick", user.getNickname());
        assertEquals("http://image", user.getProfileImageUrl());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(Role.ROLE_USER, user.getRole());
        assertNull(user.getDeletedAt());
    }

    @Test
    @DisplayName("닉네임을 정상적으로 수정하면 trim 후 값이 변경된다")
    void updateNickname_success() {
        User user = newUser();

        user.updateNickname("  newNick  ");

        assertEquals("newNick", user.getNickname());
    }

    @Test
    @DisplayName("닉네임이 공백만 있으면 BusinessException이 발생한다")
    void updateNickname_blank_throws() {
        User user = newUser();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updateNickname("   ")
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("닉네임이 최대 길이를 초과하면 BusinessException이 발생한다")
    void updateNickname_tooLong_throws() {
        User user = newUser();
        String tooLong = "a".repeat(ValidationConstant.NICKNAME_MAX_LENGTH + 1);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updateNickname(tooLong)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("닉네임에 공백 문자가 포함되면 BusinessException이 발생한다")
    void updateNickname_containsWhitespace_throws() {
        User user = newUser();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updateNickname("new Nick")
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("프로필 이미지 URL을 정상적으로 수정하면 trim 후 값이 변경된다")
    void updateProfileImageUrl_success() {
        User user = newUser();

        user.updateProfileImageUrl("  http://new-image  ");

        assertEquals("http://new-image", user.getProfileImageUrl());
    }

    @Test
    @DisplayName("프로필 이미지 URL이 공백만 있으면 BusinessException이 발생한다")
    void updateProfileImageUrl_blank_throws() {
        User user = newUser();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updateProfileImageUrl("   ")
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("암호화된 비밀번호 정상적으로 수정하면 값이 변경된다")
    void updatePasswordHash_success() {
        User user = newUser();

        user.updatePasswordHash("passwordHashNew");

        assertEquals("passwordHashNew", user.getPasswordHash());
    }

    @Test
    @DisplayName("암호화된 비밀번호가 공백만 있으면 BusinessException이 발생한다")
    void updatePasswordHash_blank_throws() {
        User user = newUser();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updatePasswordHash("   ")
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("delete는 deletedAt이 null일 때만 세팅하고, 이미 값이 있으면 변경하지 않는다")
    void delete_setsDeletedAtOnlyOnce() {
        User user = newUser();
        Instant first = Instant.parse("2025-01-01T00:00:00Z");
        Instant second = Instant.parse("2025-02-01T00:00:00Z");

        user.delete(first);
        Instant afterFirst = user.getDeletedAt();

        user.delete(second);
        Instant afterSecond = user.getDeletedAt();

        assertEquals(first, afterFirst);
        assertEquals(first, afterSecond);
    }
}
