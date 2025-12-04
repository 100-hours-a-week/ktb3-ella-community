package com.example.ktb3community.auth.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@RedisHash(value= "refresh_token", timeToLive = 604800)
public class RefreshToken{
    @Id
    private String token;

    @Indexed
    private Long userId;

    private String familyId;
}
