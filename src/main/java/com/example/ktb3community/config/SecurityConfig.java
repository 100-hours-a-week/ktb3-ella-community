package com.example.ktb3community.config;

import com.example.ktb3community.auth.security.CustomUserDetailsService;
import com.example.ktb3community.jwt.JwtAuthenticationFilter;
import com.example.ktb3community.jwt.JwtTokenProvider;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/auth/signup",
            "/auth/login",
            "/auth/refresh",
            "/users/availability/**"
    };

    private static final String[] PUBLIC_DOCS_ENDPOINTS = {
            "/v1/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] CSRF_IGNORED_ENDPOINTS = {
            "/auth/login",
            "/auth/signup",
            "/uploads/presigned-url",
            "/users/availability/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(CSRF_IGNORED_ENDPOINTS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            ErrorResponseDto body = ErrorResponseDto.of(
                                    ErrorCode.AUTH_UNAUTHORIZED.getCode(),
                                    ErrorCode.AUTH_UNAUTHORIZED.getMessage()
                            );
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ErrorResponseDto body = ErrorResponseDto.of(
                                    ErrorCode.AUTH_FORBIDDEN.getCode(),
                                    ErrorCode.AUTH_FORBIDDEN.getMessage()
                            );
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                        })
                )
                .formLogin(AbstractHttpConfigurer::disable)
                // 헤더로 아이디/비번 보내는 방식 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC_DOCS_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .userDetailsService(customUserDetailsService)
                // JWT 인증 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

