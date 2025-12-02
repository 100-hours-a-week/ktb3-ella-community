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

import java.time.Instant;
import java.util.Optional;

import static com.example.ktb3community.TestEntityFactory.*; // comment() 팩토리가 있다고 가정
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
                .role(Role.ROLE_USER)
                .build();
        jpaUserRepository.save(user);

        post = post()
                .title("Post")
                .content("Content 1")
                .user(user)
                .build();
        jpaPostRepository.save(post);
    }

    @Test
    @DisplayName("findByIdAndDeletedAtIsNull: 삭제되지 않은 댓글만 조회된다")
    void findByIdAndDeletedAtIsNull() {
        Comment activeComment = Comment.builder()
                .content("Active Comment")
                .user(user)
                .post(post)
                .build();

        Comment deletedComment = Comment.builder()
                .content("Deleted Comment")
                .user(user)
                .post(post)
                .deletedAt(Instant.now())
                .build();
        jpaCommentRepository.save(activeComment);
        jpaCommentRepository.save(deletedComment);

        Optional<Comment> foundActive = jpaCommentRepository.findByIdAndDeletedAtIsNull(activeComment.getId());
        Optional<Comment> foundDeleted = jpaCommentRepository.findByIdAndDeletedAtIsNull(deletedComment.getId());

        assertThat(foundActive).isPresent();
        assertThat(foundDeleted).isEmpty();
    }

    @Test
    @DisplayName("softDeleteByUserId: 해당 유저의 모든 댓글을 삭제하면 deletedAt이 업데이트된다")
    void softDeleteByUserId() {
        Comment comment1 = jpaCommentRepository.save(Comment.builder().content("C1").user(user).post(post).build());

        jpaCommentRepository.save(Comment.builder().content("C2").user(user).post(post).build());

        User otherUser = jpaUserRepository.save(user()
                .email("other@test.com")
                .nickname("otherUser")
                .build());

        Comment otherComment = jpaCommentRepository.save(Comment.builder().content("Other").user(otherUser).post(post).build());

        int count = jpaCommentRepository.softDeleteByUserId(user.getId(), Instant.now());

        em.flush();
        em.clear();

        assertThat(count).isEqualTo(2);

        Comment updated1 = jpaCommentRepository.findById(comment1.getId()).orElseThrow();
        Comment updatedOther = jpaCommentRepository.findById(otherComment.getId()).orElseThrow();

        assertThat(updated1.getDeletedAt()).isNotNull();
        assertThat(updatedOther.getDeletedAt()).isNull();


    }

    @Test
    @DisplayName("softDeleteByPostId: 게시글 ID로 댓글을 삭제하며, 이미 삭제된 댓글은 건드리지 않는다")
    void softDeleteByPostId_checks_deletedAt_isNull() {
        Comment activeComment = jpaCommentRepository.save(Comment.builder().content("Active").user(user).post(post).build());

        Instant oldDeletedAt = Instant.now().minusSeconds(10000);
        Comment alreadyDeletedComment = jpaCommentRepository.save(Comment.builder()
                .content("Already Deleted")
                .user(user)
                .post(post)
                .deletedAt(oldDeletedAt)
                .build());
        int count = jpaCommentRepository.softDeleteByPostId(post.getId(), Instant.now());

        em.flush();
        em.clear();

        assertThat(count).isEqualTo(1);

        Comment resultActive = jpaCommentRepository.findById(activeComment.getId()).orElseThrow();
        Comment resultAlreadyDeleted = jpaCommentRepository.findById(alreadyDeletedComment.getId()).orElseThrow();

        assertThat(resultActive.getDeletedAt()).isNotNull();

        assertThat(resultAlreadyDeleted.getDeletedAt()).isEqualTo(oldDeletedAt);
    }
}