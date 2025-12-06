package com.example.ktb3community.user;

import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.constants.ValidationConstant;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("createNew: 입력값을 trim하고 email은 소문자로 변환하여 생성한다")
    void createNew_success() {
        String email = "  TEST@Email.Com  ";
        String passwordHash = "hash";
        String nickname = "  Nick  ";
        String profileImageUrl = "  http://image  ";

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
    @DisplayName("updateNickname: 정상적인 닉네임으로 변경 시 trim되어 반영된다")
    void updateNickname_success() {
        User user = user().build();

        user.updateNickname("  newNick  ");

        assertEquals("newNick", user.getNickname());
    }

    @Test
    @DisplayName("updateNickname: 공백만 있는 경우 예외가 발생한다")
    void updateNickname_blank_throws() {
        User user = user().build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updateNickname("   ")
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateNickname: 길이 초과 시 예외가 발생한다")
    void updateNickname_tooLong_throws() {
        User user = user().build();
        String tooLong = "a".repeat(ValidationConstant.NICKNAME_MAX_LENGTH + 1);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updateNickname(tooLong)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateNickname: 공백 포함 시 예외가 발생한다")
    void updateNickname_containsWhitespace_throws() {
        User user = user().build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updateNickname("new Nick")
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateNickname: 기존 닉네임과 동일한 경우 넘어간다")
    void updateNickname_sameNick_noChange() {
        User user = user().nickname("sameNick").build();

        user.updateNickname("sameNick");

        assertEquals("sameNick", user.getNickname());
    }

    @Test
    @DisplayName("updateProfileImageUrl: 정상 변경 시 trim되어 반영된다")
    void updateProfileImageUrl_success() {
        User user = user().build();

        user.updateProfileImageUrl("  http://new-image  ");

        assertEquals("http://new-image", user.getProfileImageUrl());
    }

    @Test
    @DisplayName("updateProfileImageUrl: 공백일 경우 예외가 발생한다")
    void updateProfileImageUrl_blank_throws() {
        User user = user().build();

        Throwable thrown = catchThrowable(() -> user.updateProfileImageUrl("   "));

        Assertions.assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("updateProfileImageUrl: 기존 이미지 url과 동일한 경우 넘어간다")
    void updateProfileImageUrl_sameUrl_noChange() {
        User user = user().profileImageUrl("http://same-image").build();

        user.updateProfileImageUrl("http://same-image");

        assertEquals("http://same-image", user.getProfileImageUrl());
    }

    @Test
    @DisplayName("updatePasswordHash: 정상 변경 확인")
    void updatePasswordHash_success() {
        User user = user().build();

        user.updatePasswordHash("passwordHashNew");

        assertEquals("passwordHashNew", user.getPasswordHash());
    }

    @ParameterizedTest
    @DisplayName("updatePasswordHash: 암호화된 비밀번호가 공백이거나 null일 경우 예외가 발생한다")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void updatePasswordHash_blank_throws(String invalid) {
        User user = user().build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> user.updatePasswordHash(invalid)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("delete: 탈퇴 시 deletedAt이 설정되며, 중복 호출되어도 시간은 변경되지 않는다")
    void delete_setsDeletedAtOnlyOnce() {
        User user = user().build();
        Instant first = Instant.parse("2025-01-01T00:00:00Z");
        Instant second = Instant.parse("2025-02-01T00:00:00Z");

        user.delete(first);
        Instant afterFirst = user.getDeletedAt();

        user.delete(second);
        Instant afterSecond = user.getDeletedAt();

        assertEquals(first, afterFirst);
        assertEquals(first, afterSecond);
    }

    @Test
    @DisplayName("equals & hashCode: ID가 같은 두 객체는 동등하다")
    void equals_sameId_isEqual() {
        User user1 = user().build();
        ReflectionTestUtils.setField(user1, "id", 1L);

        User user2 = user().build();
        ReflectionTestUtils.setField(user2, "id", 1L);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("equals: ID가 다르면 다른 객체다")
    void equals_differentId_isNotEqual() {
        User user1 = user().build();
        ReflectionTestUtils.setField(user1, "id", 1L);

        User user2 = user().build();
        ReflectionTestUtils.setField(user2, "id", 2L);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("equals: ID가 null인 비영속 객체는 필드 값이 같아도 동등하지 않다")
    void equals_nullId_isNotEqual() {
        User user1 = user().id(null).build();
        User user2 = user().id(null).build();

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("equals: 자기 자신과는 항상 동등하다")
    void equals_self_isEqual() {
        User user = user().build();
        ReflectionTestUtils.setField(user, "id", 1L);

        assertThat(user).isEqualTo(user);
    }

    @Test
    @DisplayName("equals: 프록시 객체와 비교해도 ID가 같으면 동등하다")
    void equals_proxy_isEqual() {
        User original = user().build();
        ReflectionTestUtils.setField(original, "id", 1L);

        User proxy = new User() {
            @Override
            public Long getId() {
                return 1L;
            }
        };

        assertThat(original).isEqualTo(proxy);
    }

    @Test
    @DisplayName("equals: User가 아닌 타입 또는 null과는 동등하지 않다")
    void equals_nonUser_andNull_isNotEqual() {
        User user = user().build();
        ReflectionTestUtils.setField(user, "id", 1L);

        assertThat(user).isNotEqualTo("not a user");

        assertThat(user == null).isFalse();
    }
}
