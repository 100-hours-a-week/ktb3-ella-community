package com.example.ktb3community.auth;

import com.example.ktb3community.auth.domain.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static com.example.ktb3community.TestFixtures.TOKEN_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    @DisplayName("token, userId, familyId 필드가 정상적으로 매핑된다")
    void builder_success() {
        String familyId = "family-123";

        RefreshToken token = RefreshToken.builder()
                .token(TOKEN_ID)
                .userId(USER_ID)
                .familyId(familyId)
                .build();

        assertThat(token.getToken()).isEqualTo(TOKEN_ID);
        assertThat(token.getUserId()).isEqualTo(USER_ID);
        assertThat(token.getFamilyId()).isEqualTo(familyId);
    }
}
