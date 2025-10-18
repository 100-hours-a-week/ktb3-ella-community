package com.example.ktb3community.common.constants;

public final class ValidationConstant {

    private ValidationConstant() {}

    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final int PASSWORD_MIN_LENGTH = 8;

    public static final int NICKNAME_MAX_LENGTH = 10;


    public static final int TITLE_MAX_LENGTH = 26;

    public static final String NICKNAME_PATTERN_NO_SPACE = "^\\S+$";

    public static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[~!@#$%^&*()_\\-+={}\\[\\]|\\\\:;\"'<>,.?/]).{"
                    + PASSWORD_MIN_LENGTH + "," + PASSWORD_MAX_LENGTH + "}$";
}
