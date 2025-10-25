package com.example.ktb3community.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {
    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("KTB3 ella Community API")
                .version("v1")
                .description("ella Community API 문서"));
    }
}
