package com.example.ktb3community.comment;

import com.example.ktb3community.common.Role;
import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.repository.JpaCommentRepository;
import com.example.ktb3community.config.JpaConfig;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.repository.JpaPostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.JpaUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Optional;

import static com.example.ktb3community.TestEntityFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class JpaCommentRepositoryTest {

    @Autowired
    private JpaCommentRepository jpaCommentRepository;

    @Autowired
    private JpaPostRepository jpaPostRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = user()
                .email("user@test.com")
                .nickname("user")
                .role(Role.ROLE_USER)
                .build();
        jpaUserRepository.save(user);

        post = post(user)
                .title("Post")
                .content("Content 1")
                .build();
        jpaPostRepository.save(post);
    }

    @Test
    @DisplayName("findByIdAndDeletedAtIsNull: 삭제되지 않은 댓글만 조회된다")
    void findByIdAndDeletedAtIsNull() {
        Comment activeComment = jpaCommentRepository.save(
                Comment.builder()
                        .content("Active Comment")
                        .user(user)
                        .post(post)
                        .build()
        );

        Comment deletedComment = jpaCommentRepository.save(
                Comment.builder()
                        .content("Deleted Comment")
                        .user(user)
                        .post(post)
                        .deletedAt(Instant.now())
                        .build()
        );

        Optional<Comment> foundActive =
                jpaCommentRepository.findByIdAndDeletedAtIsNull(activeComment.getId());
        Optional<Comment> foundDeleted =
                jpaCommentRepository.findByIdAndDeletedAtIsNull(deletedComment.getId());

        assertThat(foundActive).isPresent();
        assertThat(foundDeleted).isEmpty();
    }

    @Test
    @DisplayName("findByPost_IdAndDeletedAtIsNull: 게시글 ID로 삭제되지 않은 댓글만 페이징 조회한다")
    void findByPost_IdAndDeletedAtIsNull() {
        Comment c1 = jpaCommentRepository.save(
                Comment.builder()
                        .content("C1")
                        .user(user)
                        .post(post)
                        .build()
        );
        Comment c2 = jpaCommentRepository.save(
                Comment.builder()
                        .content("C2")
                        .user(user)
                        .post(post)
                        .build()
        );
        jpaCommentRepository.save(
                Comment.builder()
                        .content("Deleted")
                        .user(user)
                        .post(post)
                        .deletedAt(Instant.now())
                        .build()
        );

        Page<Comment> page = jpaCommentRepository.findByPost_IdAndDeletedAtIsNull(
                post.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(Comment::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    @DisplayName("softDeleteByUserId: 해당 유저의 모든 댓글을 삭제하면 deletedAt이 업데이트된다")
    void softDeleteByUserId() {
        Comment comment1 = jpaCommentRepository.save(
                Comment.builder().content("C1").user(user).post(post).build()
        );
        Comment comment2 = jpaCommentRepository.save(
                Comment.builder().content("C2").user(user).post(post).build()
        );

        User otherUser = jpaUserRepository.save(
                user()
                        .email("other@test.com")
                        .nickname("otherUser")
                        .role(Role.ROLE_USER)
                        .build()
        );
        Comment otherComment = jpaCommentRepository.save(
                Comment.builder().content("Other").user(otherUser).post(post).build()
        );

        Instant now = Instant.now();

        int count = jpaCommentRepository.softDeleteByUserId(user.getId(), now);

        em.flush();
        em.clear();

        assertThat(count).isEqualTo(2);

        Comment updated1 = jpaCommentRepository.findById(comment1.getId()).orElseThrow();
        Comment updated2 = jpaCommentRepository.findById(comment2.getId()).orElseThrow();
        Comment updatedOther = jpaCommentRepository.findById(otherComment.getId()).orElseThrow();

        assertThat(updated1.getDeletedAt()).isNotNull();
        assertThat(updated2.getDeletedAt()).isNotNull();
        assertThat(updatedOther.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("softDeleteByPostId: 게시글 ID로 댓글을 삭제하며, 이미 삭제된 댓글은 건드리지 않는다")
    void softDeleteByPostId_checks_deletedAt_isNull() {
        // given
        Comment activeComment = jpaCommentRepository.save(
                Comment.builder()
                        .content("Active")
                        .user(user)
                        .post(post)
                        .build()
        );

        Instant oldDeletedAt = Instant.now().minusSeconds(10_000);
        Comment alreadyDeletedComment = jpaCommentRepository.save(
                Comment.builder()
                        .content("Already Deleted")
                        .user(user)
                        .post(post)
                        .deletedAt(oldDeletedAt)
                        .build()
        );

        Instant now = Instant.now();

        int count = jpaCommentRepository.softDeleteByPostId(post.getId(), now);

        em.flush();
        em.clear();

        assertThat(count).isEqualTo(1);

        Comment resultActive =
                jpaCommentRepository.findById(activeComment.getId()).orElseThrow();
        Comment resultAlreadyDeleted =
                jpaCommentRepository.findById(alreadyDeletedComment.getId()).orElseThrow();

        assertThat(resultActive.getDeletedAt()).isNotNull();
        assertThat(resultAlreadyDeleted.getDeletedAt()).isEqualTo(oldDeletedAt);
    }
}