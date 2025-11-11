package com.example.ktb3community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Ktb3CommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ktb3CommunityApplication.class, args);
    }

}
