package com.example.ktb3community.user;

import com.example.ktb3community.auth.service.RefreshTokenService;
import com.example.ktb3community.comment.repository.CommentRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.s3.service.FileService;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.dto.MeResponse;
import com.example.ktb3community.user.dto.UpdateMeRequest;
import com.example.ktb3community.user.dto.UpdatePasswordRequest;
import com.example.ktb3community.user.mapper.UserMapper;
import com.example.ktb3community.user.repository.UserRepository;
import com.example.ktb3community.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    FileService fileService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("getAvailability는 입력값을 trim하여 email/nickname 중복 여부를 조회한다")
    void getAvailability_success() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(userRepository.existsByNickname("nick")).thenReturn(true);

        AvailabilityResponse response = userService.getAvailability("  USER@test.com  ", "  nick  ");

        assertTrue(response.emailAvailable());
        assertFalse(response.nicknameAvailable());

        // 호출 검증
        verify(userRepository).existsByEmail("user@test.com");
        verify(userRepository).existsByNickname("nick");
    }

    @ParameterizedTest
    @DisplayName("getAvailability는 값이 없거나 공백이면 null을 반환한다")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void getAvailability_nullOrBlank(String invalid) {

        AvailabilityResponse response = userService.getAvailability(invalid, invalid);

        assertNull(response.emailAvailable());
        assertNull(response.nicknameAvailable());

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateMe는 닉네임이 변경될 경우 중복을 확인 후 변경하고, 프로필 이미지 변경 여부에 따라 파일 정리를 요청한다")
    void updateMe_nicknameChanged_profileImageChanged_success() {
        Long userId = 1L;
        UpdateMeRequest request = new UpdateMeRequest("newNick", "newImage");
        User user = mock(User.class);

        when(user.getNickname()).thenReturn("oldNick");
        when(user.getProfileImageUrl()).thenReturn("oldImage");

        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);
        when(userRepository.existsByNickname("newNick")).thenReturn(false);

        MeResponse meResponse = new MeResponse("user@test.com", "newNick", "newImage");
        when(userMapper.userToMeResponse(user)).thenReturn(meResponse);

        MeResponse result = userService.updateMe(userId, request);

        verify(userRepository).findByIdOrThrow(userId);
        verify(userRepository).existsByNickname("newNick");
        verify(user).updateProfileImageUrl("newImage");
        verify(fileService).deleteImageIfChanged("oldImage", "newImage");
        verify(userMapper).userToMeResponse(user);

        assertSame(meResponse, result);
    }

    @Test
    @DisplayName("updateMe는 다른 사용자가 이미 사용 중인 닉네임으로 변경하려 하면 예외를 던진다")
    void updateMe_nicknameDuplicate_throws() {
        Long userId = 1L;
        UpdateMeRequest request = new UpdateMeRequest("existingNick", null);
        User user = mock(User.class);

        when(user.getNickname()).thenReturn("oldNick");
        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);
        when(userRepository.existsByNickname("existingNick")).thenReturn(true);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.updateMe(userId, request)
        );

        assertEquals(ErrorCode.NICKNAME_ALREADY_EXIST, ex.getErrorCode());

        verify(userRepository).findByIdOrThrow(userId);
        verify(userRepository).existsByNickname("existingNick");
        verifyNoMoreInteractions(user);
        verifyNoInteractions(fileService);
        verifyNoInteractions(userMapper);
    }

    @ParameterizedTest
    @DisplayName("updateMe는 닉네임/프로필 이미지가 null 또는 공백일 때 닉네임/프로필 관련 로직을 건너뛴다")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void updateMe_whenNullOrBlank_skip(String invalid) {
        Long userId = 1L;

        UpdateMeRequest request = new UpdateMeRequest(invalid, invalid);

        User user = mock(User.class);
        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);


        userService.updateMe(userId, request);

        verify(userRepository).findByIdOrThrow(userId);
        verifyNoMoreInteractions(user);
        verifyNoInteractions(fileService);
    }

    @Test
    @DisplayName("updateMe는 닉네임이 현재 닉네임과 동일할 때 닉네임 관련 로직을 건너뛴다")
    void updateMe_whenNicknameSameAsCurrent_skip() {
        Long userId = 1L;

        UpdateMeRequest req = new UpdateMeRequest("sameNick", null);

        User user = mock(User.class);
        when(user.getNickname()).thenReturn("sameNick");
        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);

        userService.updateMe(userId, req);

        verify(userRepository, never()).existsByNickname(any());
        verify(user, never()).updateNickname(any());
    }

    @Test
    @DisplayName("updateMe는 중복된 닉네임으로 변경하려 할 때 예외를 던진다")
    void updateMe_whenNicknameDuplicate_throws() {
        Long userId = 1L;

        UpdateMeRequest req = new UpdateMeRequest("dupNick", null);

        User user = mock(User.class);
        when(user.getNickname()).thenReturn("oldNick");
        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);
        when(userRepository.existsByNickname("dupNick")).thenReturn(true);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.updateMe(userId, req)
        );
        assertEquals(ErrorCode.NICKNAME_ALREADY_EXIST, ex.getErrorCode());
    }

    @Test
    @DisplayName("updatePassword는 비밀번호를 해시화하여 변경한다")
    void updatePassword_success() {
        Long userId = 1L;
        UpdatePasswordRequest request = new UpdatePasswordRequest("newPassword");

        User user = mock(User.class);
        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedPassword");

        userService.updatePassword(userId, request);

        verify(userRepository).findByIdOrThrow(userId);
        verify(passwordEncoder).encode("newPassword");
        verify(user).updatePasswordHash("hashedPassword");
    }

    @Test
    @DisplayName("withdrawMe는 회원 탈퇴 시 관련 데이터를 모두 소프트 삭제하고 리프레시 토큰을 폐기한다")
    void withdrawMe_success() {
        Long userId = 1L;
        User user = mock(User.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);

        userService.withdrawMe(userId, response);

        verify(userRepository).findByIdOrThrow(userId);
        verify(commentRepository).softDeleteByUserId(eq(userId), any());
        verify(postRepository).softDeleteByUserId(eq(userId), any());
        verify(userRepository).softDeleteById(eq(userId), any());
        verify(refreshTokenService).revokeAllByUser(user);
    }
}
