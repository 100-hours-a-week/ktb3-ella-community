package com.example.ktb3community.post.like;

import com.example.ktb3community.post.domain.Like;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.example.ktb3community.TestEntityFactory.like;
import static com.example.ktb3community.TestEntityFactory.post;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {

    @Test
    @DisplayName("createNew: 좋아요 생성 시 deletedAt은 null이다")
    void createNew_success() {
        Post post = post().id(1L).build();
        User user = user().id(1L).build();

        Like like = Like.createNew(post, user);

        assertThat(like.getPost()).isEqualTo(post);
        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getDeletedAt()).isNull();
        assertThat(like.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("delete: 좋아요 취소 시 deletedAt이 현재 시간으로 설정된다")
    void delete_success() {
        Like like = like(post().id(1L).build(), user().id(1L).build()).build();
        Instant now = Instant.now();

        like.delete(now);

        assertThat(like.isDeleted()).isTrue();
        assertThat(like.getDeletedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("delete: 이미 취소된 좋아요를 다시 취소해도 시간은 변경되지 않는다")
    void delete_idempotent() {
        Like like = like(post().id(1L).build(), user().id(1L).build()).build();
        Instant firstTime = Instant.parse("2025-01-01T10:00:00Z");
        Instant secondTime = Instant.parse("2025-01-01T12:00:00Z");

        like.delete(firstTime);
        like.delete(secondTime);

        assertThat(like.getDeletedAt()).isEqualTo(firstTime);
    }

    @Test
    @DisplayName("restore: 삭제된 좋아요를 복구하면 deletedAt이 null이 된다")
    void restore_success() {
        Like like = like(post().id(1L).build(), user().id(1L).build()).build();
        like.delete(Instant.now());
        assertThat(like.isDeleted()).isTrue();

        like.restore();

        assertThat(like.isDeleted()).isFalse();
        assertThat(like.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("restore: 이미 활성화된 좋아요를 복구해도 변화가 없다")
    void restore_idempotent() {
        Like like = like(post().id(1L).build(), user().id(1L).build()).build();

        like.restore();

        assertThat(like.isDeleted()).isFalse();
        assertThat(like.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("isDeleted: deletedAt 값 유무에 따른 상태 반환 확인")
    void isDeleted_check() {
        Like like = like(post().build(), user().build()).build();

        assertThat(like.isDeleted()).isFalse();

        like.delete(Instant.now());
        assertThat(like.isDeleted()).isTrue();

        like.restore();
        assertThat(like.isDeleted()).isFalse();
    }
}
