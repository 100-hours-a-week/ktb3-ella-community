package com.example.ktb3community.post;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.user.domain.User;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static com.example.ktb3community.TestFixtures.USER_ID;
import static com.example.ktb3community.TestEntityFactory.post;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;


class PostTest {

    @Test
    @DisplayName("createNew: 제목은 trim되고 초기 카운트는 0으로 생성된다")
    void createNew_success() {
        User user = user().id(USER_ID).build();
        String title = "  Trimmed Title  ";
        String content = "Content";
        String imageUrl = "http://image.url";

        Post post = Post.createNew(user, title, content, imageUrl);

        assertThat(post.getTitle()).isEqualTo("Trimmed Title");
        assertThat(post.getContent()).isEqualTo("Content");
        assertThat(post.getPostImageUrl()).isEqualTo("http://image.url");
        assertThat(post.getUser()).isEqualTo(user);

        assertThat(post.getLikeCount()).isZero();
        assertThat(post.getViewCount()).isZero();
        assertThat(post.getCommentCount()).isZero();
        assertThat(post.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("updatePost: 제목과 내용은 값이 있을 때만 수정되고, 이미지는 무조건 수정된다")
    void updatePost_success() {
        Post post = post().title("Original Title").content("Original Content").postImageUrl("http://original.jpg").build();

        post.updatePost("  New Title  ", "New Content", "http://new.jpg");

        assertThat(post.getTitle()).isEqualTo("New Title");
        assertThat(post.getContent()).isEqualTo("New Content");
        assertThat(post.getPostImageUrl()).isEqualTo("http://new.jpg");
    }

    @Test
    @DisplayName("updatePost: 제목과 내용이 null이거나 공백이면 기존 값을 유지한다")
    void updatePost_ignoreNullOrBlank() {
        Post post = post().title("Original Title").content("Original Content").postImageUrl("http://original.jpg").build();
        String originalTitle = post.getTitle();
        String originalContent = post.getContent();

        post.updatePost(null, "   ", "http://changed.jpg");

        assertThat(post.getTitle()).isEqualTo(originalTitle);
        assertThat(post.getContent()).isEqualTo(originalContent);
        assertThat(post.getPostImageUrl()).isEqualTo("http://changed.jpg");
    }

    @Test
    @DisplayName("updatePost: 이미지는 null이 들어오면 null로 업데이트된다")
    void updatePost_imageCanBeNull() {
        Post post = post().postImageUrl("http://original.jpg").build();

        post.updatePost(null, null, null);

        assertThat(post.getPostImageUrl()).isNull();
    }

    @Test
    @DisplayName("increaseViewCount: 조회수가 1 증가한다")
    void increaseViewCount() {
        Post post = post().build();

        post.increaseViewCount();

        assertThat(post.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("LikeCount: 좋아요 증가 및 감소 (0 이하로 내려가지 않아야한다)")
    void likeCount_logic() {
        Post post = post().build();

        post.increaseLikeCount();
        assertThat(post.getLikeCount()).isEqualTo(1);

        post.decreaseLikeCount();
        assertThat(post.getLikeCount()).isZero();

        post.decreaseLikeCount();
        assertThat(post.getLikeCount()).isZero();
    }

    @Test
    @DisplayName("CommentCount: 댓글 수 증가 및 감소 (0 이하로 내려가지 않아야한다)")
    void commentCount_logic() {
        Post post = post().build();

        post.increaseCommentCount();
        assertThat(post.getCommentCount()).isEqualTo(1);

        post.decreaseCommentCount();
        assertThat(post.getCommentCount()).isZero();

        post.decreaseCommentCount();
        assertThat(post.getCommentCount()).isZero();
    }

    @Test
    @DisplayName("delete: 삭제 시간을 설정하며 멱등성을 보장한다")
    void delete_idempotent() {
        Post post = post().build();
        Instant now = Instant.now();

        post.delete(now);
        assertThat(post.getDeletedAt()).isEqualTo(now);

        post.delete(now.plusSeconds(100));
        assertThat(post.getDeletedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("getUserId: User가 존재하면 ID 반환")
    void getUserId_success() {
        User user = user().id(USER_ID).build();
        Post post = post(user).build();

        assertThat(post.getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("getUserId: User가 null이면 예외 발생")
    void getUserId_nullUser_throws() {
        Post post = Post.builder().user(null).build();

        Throwable thrown = catchThrowable(post::getUserId);

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("equals & hashCode: ID가 같으면 동등한 객체다")
    void equals_identity() {
        Post post1 = Post.builder().build();
        ReflectionTestUtils.setField(post1, "id", 1L);

        Post post2 = Post.builder().build();
        ReflectionTestUtils.setField(post2, "id", 1L);

        assertThat(post1).isEqualTo(post2);
        assertThat(post1.hashCode()).isEqualTo(post2.hashCode());
    }

    @Test
    @DisplayName("equals: ID가 다르면 다른 객체다")
    void equals_differentId_isNotEqual() {
        Post post1 = post().build();
        ReflectionTestUtils.setField(post1, "id", 1L);

        Post post2 = post().build();
        ReflectionTestUtils.setField(post2, "id", 2L);

        AssertionsForClassTypes.assertThat(post1).isNotEqualTo(post2);
    }

    @Test
    @DisplayName("equals: ID가 null인 비영속 객체는 필드 값이 같아도 동등하지 않다")
    void equals_nullId_isNotEqual() {
        Post post1 = post().id(null).build();
        Post post2 = post().id(null).build();

        AssertionsForClassTypes.assertThat(post1).isNotEqualTo(post2);
    }

    @Test
    @DisplayName("equals: 자기 자신과는 항상 동등하다")
    void equals_self_isEqual() {
        Post post = post().build();
        ReflectionTestUtils.setField(post, "id", 1L);

        AssertionsForClassTypes.assertThat(post).isEqualTo(post);
    }

    @Test
    @DisplayName("equals: 프록시 객체와 비교해도 ID가 같으면 동등하다")
    void equals_proxy() {
        Post original = Post.builder().build();
        ReflectionTestUtils.setField(original, "id", 100L);

        Post proxy = new Post() {
            @Override
            public Long getId() {
                return 100L;
            }
        };

        assertThat(original).isEqualTo(proxy);
    }
}
