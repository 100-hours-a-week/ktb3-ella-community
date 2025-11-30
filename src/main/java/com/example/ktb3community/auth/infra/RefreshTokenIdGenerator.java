package com.example.ktb3community.auth.infra;

import com.github.f4b6a3.tsid.TsidCreator;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenIdGenerator {
    public long generate() {
        return TsidCreator.getTsid().toLong();
    }
}
