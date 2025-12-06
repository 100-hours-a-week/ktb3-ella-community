package com.example.ktb3community.jwt;

import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.auth.security.CustomUserDetailsService;
import com.example.ktb3community.auth.security.SecurityPaths;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock CustomUserDetailsService userDetailsService;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("헤더에 Authorization이 없으면 인증 없이 다음 필터로 진행한다")
    void doFilterInternal_noHeader_continueChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Bearer 토큰 형식이 아니면 인증 없이 다음 필터로 진행한다")
    void doFilterInternal_invalidHeaderFormat_continueChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic aw3d523");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("유효한 토큰이 있으면 인증 객체를 SecurityContext에 저장하고 다음 필터로 진행한다")
    void doFilterInternal_validToken_authenticates() throws ServletException, IOException {
        String token = "valid.access.token";
        Long userId = 1L;
        String email = "user@test.com";
        String role = "ROLE_USER";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.getUserIdFromAccessToken(token)).willReturn(userId);
        given(jwtTokenProvider.getEmailFromAccessToken(token)).willReturn(email);
        given(jwtTokenProvider.getRoleFromAccessToken(token)).willReturn(role);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        var context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().getPrincipal())
                .isInstanceOf(CustomUserDetails.class);

        CustomUserDetails principal = (CustomUserDetails) context.getAuthentication().getPrincipal();
        assertThat(principal.getId()).isEqualTo(userId);
        assertThat(principal.getEmail()).isEqualTo(email);

        assertThat(context.getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly(role);
    }

    @Test
    @DisplayName("토큰이 유효하지 않은 경우 INVALID_ACCESS_TOKEN 예외를 던진다")
    void doFilterInternal_invalidToken_throws() {
        String token = "invalid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.getUserIdFromAccessToken(token))
                .willThrow(new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN));

        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_UNAUTHORIZED);

        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("공개된 URL(PUBLIC_AUTH)에 포함되면 필터를 실행하지 않아야 한다")
    void shouldNotFilter_publicUrl_returnsTrue() {
        String pattern = SecurityPaths.PUBLIC_AUTH[0];

        MockHttpServletRequest request = new MockHttpServletRequest("GET", pattern);
        request.setServletPath(pattern);
        boolean result = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "shouldNotFilter", request));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("공개된 URL 패턴에 매칭되지 않으면 필터를 실행해야 한다")
    void shouldNotFilter_nonPublicUrl_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        request.setServletPath("/api/protected");

        boolean result = Boolean.TRUE.equals(
                ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "shouldNotFilter", request)
        );

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("userId가 null이면 인증을 설정하지 않고 다음 필터로 진행한다")
    void doFilterInternal_nullUserId_doesNotAuthenticate() throws ServletException, IOException {
        String token = "token-with-null-user";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.getUserIdFromAccessToken(token)).willReturn(null);
        given(jwtTokenProvider.getEmailFromAccessToken(token)).willReturn("user@test.com");
        given(jwtTokenProvider.getRoleFromAccessToken(token)).willReturn("ROLE_USER");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("이미 인증 객체가 존재하면 새로운 인증으로 덮어쓰지 않는다")
    void doFilterInternal_alreadyAuthenticated_doesNotOverride() throws ServletException, IOException {
        String token = "valid.access.token";
        Long userId = 1L;
        String email = "user@test.com";
        String role = "ROLE_USER";

        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("existingUser", null, null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.getUserIdFromAccessToken(token)).willReturn(userId);
        given(jwtTokenProvider.getEmailFromAccessToken(token)).willReturn(email);
        given(jwtTokenProvider.getRoleFromAccessToken(token)).willReturn(role);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isSameAs(existingAuth);
    }

    @Test
    @DisplayName("토큰 검증 중 INVALID_ACCESS_TOKEN 이외의 BusinessException은 변환하지 않고 필터 체인을 계속 진행한다")
    void doFilterInternal_otherBusinessException_continuesChain() throws ServletException, IOException {
        String token = "token-causing-other-error";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.getUserIdFromAccessToken(token))
                .willThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
