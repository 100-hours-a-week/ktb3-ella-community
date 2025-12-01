package com.example.ktb3community.post;

import com.example.ktb3community.common.Role;
import com.example.ktb3community.config.JpaConfig;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.repository.JpaPostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.JpaUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static com.example.ktb3community.TestEntityFactory.post;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class JpaPostRepositoryTest {

    @Autowired
    private JpaPostRepository jpaPostRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("soft_delete된 게시글은 조회 메서드에서 검색되지 않아야 한다")
    void findByIdAndDeletedAtIsNull() {
        User newUser = user()
                .email("user@test.com")
                .role(Role.ROLE_USER)
                .build();
        jpaUserRepository.save(newUser);

        Post newPost = post()
                .title("Test Post")
                .content("This is a test post.")
                .user(newUser)
                .build();

        newPost.delete(Instant.now());
        Post savedPost = jpaPostRepository.save(newPost);

        Optional<Post> result = jpaPostRepository.findByIdAndDeletedAtIsNull(savedPost.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("softDeleteByUserId에서 해당 유저의 모든 게시글이 soft delete 된다")
    void softDeleteByUserId_updates_deletedAt() {

        User newUser = user()
                .email("user2@test.com")
                .role(Role.ROLE_USER)
                .build();
        User savedUser = jpaUserRepository.save(newUser);

        Post post1 = post()
                .title("Post 1")
                .content("Content 1")
                .user(savedUser)
                .build();

        Post post2 = post()
                .title("Post 2")
                .content("Content 2")
                .user(savedUser)
                .build();

        jpaPostRepository.save(post1);
        jpaPostRepository.save(post2);

        int updatedCount = jpaPostRepository.softDeleteByUserId(savedUser.getId(), Instant.now());

        em.flush();
        em.clear();

        assertThat(updatedCount).isEqualTo(2);

        Post updatedPost1 = jpaPostRepository.findById(post1.getId()).orElseThrow();
        Post updatedPost2 = jpaPostRepository.findById(post2.getId()).orElseThrow();

        assertThat(updatedPost1.getDeletedAt()).isNotNull();
        assertThat(updatedPost2.getDeletedAt()).isNotNull();

       assertThat(updatedPost1.getUpdatedAt()).isAfter(updatedPost1.getCreatedAt());
    }
}