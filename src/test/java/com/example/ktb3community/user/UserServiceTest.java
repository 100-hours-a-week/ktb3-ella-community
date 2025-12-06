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
import com.example.ktb3community.user.exception.UserNotFoundException;
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

import java.time.Instant;

import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    @Mock UserMapper userMapper;
    @Mock FileService fileService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RefreshTokenService refreshTokenService;

    @InjectMocks UserService userService;

    @Test
    @DisplayName("getAvailability: 입력값을 trim하여 중복 여부를 확인한다")
    void getAvailability_success() {
        given(userRepository.existsByEmail("user@test.com")).willReturn(false);
        given(userRepository.existsByNickname("nick")).willReturn(true);

        AvailabilityResponse response = userService.getAvailability("  USER@test.com  ", "  nick  ");

        assertThat(response.emailAvailable()).isTrue();
        assertThat(response.nicknameAvailable()).isFalse();

        verify(userRepository).existsByEmail("user@test.com");
        verify(userRepository).existsByNickname("nick");
    }

    @Test
    @DisplayName("getAvailability: email만 있을 때 email만 체크하고 nickname은 null이다")
    void getAvailability_emailOnly() {
        given(userRepository.existsByEmail("user@test.com")).willReturn(false);

        AvailabilityResponse response =
                userService.getAvailability("  user@test.com  ", "   ");

        assertThat(response.emailAvailable()).isTrue();
        assertThat(response.nicknameAvailable()).isNull();

        verify(userRepository).existsByEmail("user@test.com");
        verify(userRepository, never()).existsByNickname(anyString());
    }

    @Test
    @DisplayName("getAvailability: nickname만 있을 때 nickname만 체크하고 email은 null이다")
    void getAvailability_nicknameOnly() {
        given(userRepository.existsByNickname("nick")).willReturn(true);

        AvailabilityResponse response =
                userService.getAvailability("   ", "  nick  ");

        assertThat(response.emailAvailable()).isNull();
        assertThat(response.nicknameAvailable()).isFalse();

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).existsByNickname("nick");
    }

    @ParameterizedTest
    @DisplayName("getAvailability: 값이 없거나 공백이면 null을 반환한다")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void getAvailability_nullOrBlank(String invalid) {
        AvailabilityResponse response = userService.getAvailability(invalid, invalid);

        assertThat(response.emailAvailable()).isNull();
        assertThat(response.nicknameAvailable()).isNull();

        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("getMe: 내 정보 조회 성공")
    void getMe_Success() {
        User user = User.builder()
                .id(USER_ID)
                .nickname("test")
                .email("user@test.com")
                .build();

        MeResponse mockResponse = new MeResponse("user@test.com", "test", "http://image.url");

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(userMapper.userToMeResponse(user)).willReturn(mockResponse);

        MeResponse result = userService.getMe(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.nickname()).isEqualTo("test");
    }

    @Test
    @DisplayName("getMe: 존재하지 않는 유저 조회 시 예외가 발생한다")
    void getMe_NotFound() {
        given(userRepository.findByIdOrThrow(USER_ID))
                .willThrow(new UserNotFoundException());

        assertThatThrownBy(() -> userService.getMe(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("updateMe: 닉네임과 프로필 이미지가 정상적으로 변경된다")
    void updateMe_success() {
        UpdateMeRequest request = new UpdateMeRequest("newNick", "newImage");

        User user = User.builder()
                .id(USER_ID)
                .nickname("oldNick")
                .profileImageUrl("oldImage")
                .build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(userRepository.existsByNickname("newNick")).willReturn(false);

        MeResponse expectedResponse = new MeResponse("test@email.com", "newNick", "newImage");
        given(userMapper.userToMeResponse(user)).willReturn(expectedResponse);

        MeResponse result = userService.updateMe(USER_ID, request);

        assertThat(result).isEqualTo(expectedResponse);

        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(user.getProfileImageUrl()).isEqualTo("newImage");

        verify(fileService).deleteImageIfChanged("oldImage", "newImage");
    }

    @ParameterizedTest
    @DisplayName("updateMe: 프로필 이미지가 null이거나 공백이면 기존 이미지를 유지하고 FileService를 호출하지 않는다")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void updateMe_profileImageNull(String invalid) {
        UpdateMeRequest request = new UpdateMeRequest("newNick", invalid);

        User user = User.builder()
                .id(USER_ID)
                .nickname("oldNick")
                .profileImageUrl("oldImage")
                .build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(userRepository.existsByNickname("newNick")).willReturn(false);

        MeResponse expected = new MeResponse("test@email.com", "newNick", "oldImage");
        given(userMapper.userToMeResponse(user)).willReturn(expected);

        userService.updateMe(USER_ID, request);

        assertThat(user.getProfileImageUrl()).isEqualTo("oldImage");
        verify(fileService, never()).deleteImageIfChanged(any(), any());
    }

    @ParameterizedTest
    @DisplayName("updateMe: 닉네임이 null이면 닉네임 변경/중복 체크를 하지 않는다")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void updateMe_nicknameNull(String invalid) {
        UpdateMeRequest request = new UpdateMeRequest(invalid, "newImage");

        User user = User.builder()
                .id(USER_ID)
                .nickname("oldNick")
                .profileImageUrl("oldImage")
                .build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);

        MeResponse expected = new MeResponse("test@email.com", "oldNick", "newImage");
        given(userMapper.userToMeResponse(user)).willReturn(expected);

        userService.updateMe(USER_ID, request);

        assertThat(user.getNickname()).isEqualTo("oldNick"); // 닉네임 그대로
        verify(userRepository, never()).existsByNickname(anyString());
    }

    @Test
    @DisplayName("updateMe: 닉네임이 현재와 같으면 중복 체크를 건너뛰고 성공한다")
    void updateMe_sameNickname() {
        UpdateMeRequest request = new UpdateMeRequest("oldNick", "newImage");

        User user = User.builder()
                .id(USER_ID)
                .nickname("oldNick")
                .profileImageUrl("oldImage")
                .build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);

        MeResponse expectedResponse = new MeResponse("test@email.com", "oldNick", "newImage");
        given(userMapper.userToMeResponse(user)).willReturn(expectedResponse);

        userService.updateMe(USER_ID, request);

        verify(userRepository, never()).existsByNickname(any());
        assertThat(user.getProfileImageUrl()).isEqualTo("newImage");
    }

    @Test
    @DisplayName("updateMe: 이미 존재하는 닉네임으로 변경 시 예외가 발생한다")
    void updateMe_nicknameDuplicate_throws() {
        UpdateMeRequest request = new UpdateMeRequest("existingNick", null);
        User user = User.builder().id(USER_ID).nickname("oldNick").build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(userRepository.existsByNickname("existingNick")).willReturn(true);


        Throwable thrown = catchThrowable(() -> userService.updateMe(USER_ID, request));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_ALREADY_EXIST);

        assertThat(user.getNickname()).isEqualTo("oldNick");
    }

    @Test
    @DisplayName("updatePassword: 비밀번호 해시화 후 변경 상태를 검증한다")
    void updatePassword_success() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("newPassword");
        User user = User.builder()
                .id(USER_ID)
                .passwordHash("oldHash")
                .build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(passwordEncoder.encode("newPassword")).willReturn("hashedPassword");

        userService.updatePassword(USER_ID, request);

        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    @DisplayName("withdrawMe: 회원 탈퇴 시 소프트 삭제 및 토큰을 폐기한다")
    void withdrawMe_success() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        userService.withdrawMe(USER_ID, response);

        verify(commentRepository).softDeleteByUserId(eq(USER_ID), isA(Instant.class));
        verify(postRepository).softDeleteByUserId(eq(USER_ID), isA(Instant.class));
        verify(userRepository).softDeleteById(eq(USER_ID), isA(Instant.class));

        verify(refreshTokenService).revokeAllByUser(USER_ID);
    }
}