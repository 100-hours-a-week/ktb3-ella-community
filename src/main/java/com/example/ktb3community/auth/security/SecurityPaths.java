package com.example.ktb3community.auth.security;

public final class SecurityPaths {

    private SecurityPaths() {}

    public static final String[] PUBLIC_AUTH = {
            "/auth/signup",
            "/auth/login",
            "/auth/refresh",
            "/users/availability/**"
    };

    public static final String[] CSRF_IGNORED = {
            "/auth/login",
            "/auth/signup",
            "/uploads/presigned-url",
            "/users/availability/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    public static final String[] PUBLIC_DOCS = {
            "/v1/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
}
