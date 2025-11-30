package com.example.ktb3community.common.constants;

import java.time.Duration;

public class CookieConstant {
    private CookieConstant() {}

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final Duration REFRESH_COOKIE_MAX_AGE = Duration.ofDays(7);
    public static final String REFRESH_COOKIE_PATH = "/api/auth";
}
