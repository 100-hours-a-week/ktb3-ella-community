package com.example.ktb3community.auth;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.auth.dto.LoginRequest;
import com.example.ktb3community.auth.dto.SignUpRequest;
import com.example.ktb3community.auth.dto.Token;
import com.example.ktb3community.auth.service.AuthService;
import com.example.ktb3community.auth.service.RefreshTokenService;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock
    RefreshTokenService refreshTokenService;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    private User newUser(String email, String passwordHash) {
        User user = User.createNew(email, passwordHash, "nickname", "img", Role.ROLE_USER);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    @Test
    @DisplayName("signup: 이메일/닉네임 중복이 없으면 회원을 저장하고 토큰을 발급한다")
    void signup_success() {
        SignUpRequest request = new SignUpRequest("test@email.com", "password", "nickname", "img");
        String encodedPassword = "encodedPassword";

        given(passwordEncoder.encode("password")).willReturn(encodedPassword);
        given(userRepository.existsByEmail("test@email.com")).willReturn(false);
        given(userRepository.existsByNickname("nickname")).willReturn(false);

        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", USER_ID);
            return user;
        });

        given(jwtTokenProvider.createAccessToken(USER_ID)).willReturn("access.token");
        given(refreshTokenService.createRefreshToken(any(User.class))).willReturn("refresh.token");

        Token token = authService.signup(request);

        assertThat(token.accessToken()).isEqualTo("access.token");
        assertThat(token.refreshToken()).isEqualTo("refresh.token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("test@email.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("signup: 이미 존재하는 이메일이면 EMAIL_ALREADY_EXIST 예외 발생")
    void signup_emailDuplicate_throws() {
        SignUpRequest request = new SignUpRequest("test@email.com", "pw", "nick", "img");
        given(userRepository.existsByEmail("test@email.com")).willReturn(true);

        Throwable thrown = catchThrowable(() -> authService.signup(request));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXIST);
    }

    @Test
    @DisplayName("signup: 이미 존재하는 닉네임이면 NICKNAME_ALREADY_EXIST 예외 발생")
    void signup_nicknameDuplicate_throws() {
        // Given
        SignUpRequest request = new SignUpRequest("test@email.com", "pw", "nick", "img");
        given(userRepository.existsByEmail("test@email.com")).willReturn(false);
        given(userRepository.existsByNickname("nick")).willReturn(true);

        Throwable thrown = catchThrowable(() -> authService.signup(request));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_ALREADY_EXIST);
    }

    @Test
    @DisplayName("login: 이메일과 비밀번호가 일치하면 토큰을 발급한다")
    void login_success() {
        LoginRequest request = new LoginRequest("test@email.com", "password");
        User user = newUser("test@email.com", "encodedPassword");

        given(userRepository.findByEmail("test@email.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password", "encodedPassword")).willReturn(true);

        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access.token");
        given(refreshTokenService.createRefreshToken(user)).willReturn("refresh.token");

        Token token = authService.login(request);

        assertThat(token.accessToken()).isEqualTo("access.token");
        assertThat(token.refreshToken()).isEqualTo("refresh.token");
    }

    @Test
    @DisplayName("login: 존재하지 않는 이메일이면 UserNotFoundException 발생")
    void login_emailNotFound_throws() {
        LoginRequest request = new LoginRequest("unknown@email.com", "pw");
        given(userRepository.findByEmail("unknown@email.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("login: 비밀번호가 일치하지 않으면 UserNotFoundException 발생")
    void login_wrongPassword_throws() {
        LoginRequest request = new LoginRequest("test@email.com", "wrongPw");
        User user = newUser("test@email.com", "encodedPassword");

        given(userRepository.findByEmail("test@email.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPw", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("refresh: 유효한 리프레시 토큰으로 액세스 토큰 재발급 및 리프레시 토큰 교체(Rotate)")
    void refresh_success() {
        String oldRefreshToken = "old.refresh.token";
        String newRefreshToken = "new.refresh.token";
        String newAccessToken = "new.access.token";
        User user = newUser("email", "pw");

        RefreshToken validToken = RefreshToken.createNew(100L, user, null);
        given(refreshTokenService.getValidTokenOrThrow(oldRefreshToken)).willReturn(validToken);

        given(jwtTokenProvider.createAccessToken(user.getId())).willReturn(newAccessToken);
        given(refreshTokenService.rotate(oldRefreshToken)).willReturn(newRefreshToken);

        Token token = authService.refresh(oldRefreshToken);

        assertThat(token.accessToken()).isEqualTo(newAccessToken);
        assertThat(token.refreshToken()).isEqualTo(newRefreshToken);

        verify(refreshTokenService).getValidTokenOrThrow(oldRefreshToken);
        verify(refreshTokenService).rotate(oldRefreshToken);
    }

    @Test
    @DisplayName("logout: 리프레시 토큰을 폐기한다")
    void logout_success() {
        String refreshToken = "target.token";

        authService.logout(refreshToken);

        verify(refreshTokenService).revoke(refreshToken);
    }
}