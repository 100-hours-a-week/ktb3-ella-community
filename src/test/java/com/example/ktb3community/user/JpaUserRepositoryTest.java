package com.example.ktb3community.user;

import com.example.ktb3community.common.Role;
import com.example.ktb3community.config.JpaConfig;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class JpaUserRepositoryTest {
    @Autowired
    private JpaUserRepository jpaUserRepository;
    @Test
    @DisplayName("soft_delete된 유저는 조회 메서드에서 검색되지 않아야 한다.")
    void findByEmailAndDeletedAtIsNull() {
        User deletedUser = user()
                .id(null)
                .email("user@test.com")
                .passwordHash("hash")
                .nickname("deletedUser")
                .profileImageUrl("http://image")
                .role(Role.ROLE_USER)
                .build();

        jpaUserRepository.save(deletedUser);
        jpaUserRepository.softDeleteById(deletedUser.getId(), java.time.Instant.now());

        Optional<User> result = jpaUserRepository.findByEmailAndDeletedAtIsNull("user@test.com");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("softDeleteById에서 deletedAt, updatedAt이 갱신된다")
    void softDeleteById_updates_deletedAt() {

        User user = user()
                .id(null)
                .email("user@test.com")
                .passwordHash("hash")
                .nickname("deletedUser")
                .profileImageUrl("http://image")
                .role(Role.ROLE_USER)
                .build();
        User savedUser = jpaUserRepository.save(user);

        int updatedCount = jpaUserRepository.softDeleteById(savedUser.getId(), Instant.now());

        assertThat(updatedCount).isEqualTo(1);

        User updatedUser = jpaUserRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getDeletedAt()).isNotNull();
        assertThat(updatedUser.getUpdatedAt()).isAfter(savedUser.getCreatedAt());
    }
}
