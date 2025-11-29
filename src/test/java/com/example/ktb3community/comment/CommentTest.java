package com.example.ktb3community.comment;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static com.example.ktb3community.TestFixtures.POST_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentTest {

    private User newUser() {
        User user = User.builder()
                .email("test@email.com")
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    private Post newPost() {
        Post post = Post.builder()
                .title("Post Title")
                .build();
        ReflectionTestUtils.setField(post, "id", POST_ID);
        return post;
    }

    @Test
    @DisplayName("createNew: 댓글 생성 시 내용과 연관관계가 설정되고 deletedAt은 null이다")
    void createNew_success() {
        User user = newUser();
        Post post = newPost();
        String content = "Nice comment!";

        Comment comment = Comment.createNew(post, user, content);

        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getUser()).isEqualTo(user);
        assertThat(comment.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("updateContent: 내용이 null이거나 공백이 아니면 수정된다")
    void updateContent_success() {
        Comment comment = Comment.createNew(newPost(), newUser(), "Old Content");

        comment.updateContent("New Content");

        assertThat(comment.getContent()).isEqualTo("New Content");
    }

    @Test
    @DisplayName("updateContent: 내용이 null이거나 공백이면 수정되지 않는다")
    void updateContent_ignoreInvalid() {
        String initialContent = "Original Content";
        Comment comment = Comment.createNew(newPost(), newUser(), initialContent);

        comment.updateContent(null);
        assertThat(comment.getContent()).isEqualTo(initialContent);

        comment.updateContent("   ");
        assertThat(comment.getContent()).isEqualTo(initialContent);
    }

    @Test
    @DisplayName("delete: 삭제 시간을 설정하며 멱등성을 보장한다")
    void delete_success() {
        Comment comment = Comment.createNew(newPost(), newUser(), "Content");
        Instant now = Instant.now();

        comment.delete(now);
        assertThat(comment.getDeletedAt()).isEqualTo(now);

        comment.delete(now.plusSeconds(100));
        assertThat(comment.getDeletedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("getUserId: User가 존재하면 ID를 반환한다")
    void getUserId_success() {
        User user = newUser();
        Comment comment = Comment.createNew(newPost(), user, "Content");

        assertThat(comment.getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("getUserId: User가 null이면 UserNotFoundException을 던진다")
    void getUserId_null_throws() {
        Comment comment = Comment.builder()
                .post(newPost())
                .user(null)
                .content("Content")
                .build();

        assertThatThrownBy(comment::getUserId)
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getPostId: Post가 존재하면 ID를 반환한다")
    void getPostId_success() {
        Post post = newPost();
        Comment comment = Comment.createNew(post, newUser(), "Content");

        assertThat(comment.getPostId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("getPostId: Post가 null이면 PostNotFoundException을 던진다")
    void getPostId_null_throws() {
        Comment comment = Comment.builder()
                .post(null)
                .user(newUser())
                .content("Content")
                .build();

        assertThatThrownBy(comment::getPostId)
                .isInstanceOf(PostNotFoundException.class);
    }
    @Test
    @DisplayName("equals & hashCode: ID가 같으면 동등한 객체다")
    void equals_identity() {
        Comment comment1 = Comment.builder().build();
        ReflectionTestUtils.setField(comment1, "id", 1L);

        Comment comment2 = Comment.builder().build();
        ReflectionTestUtils.setField(comment2, "id", 1L);

        assertThat(comment1).isEqualTo(comment2);
        assertThat(comment1.hashCode()).isEqualTo(comment2.hashCode());
    }

    @Test
    @DisplayName("equals: ID가 다르면 다른 객체다")
    void equals_differentId_isNotEqual() {
        Comment comment1 = Comment.builder().build();
        ReflectionTestUtils.setField(comment1, "id", 1L);

        Comment comment2 = Comment.builder().build();
        ReflectionTestUtils.setField(comment2, "id", 2L);

        AssertionsForClassTypes.assertThat(comment1).isNotEqualTo(comment2);
    }

    @Test
    @DisplayName("equals: ID가 null인 비영속 객체는 필드 값이 같아도 동등하지 않다")
    void equals_nullId_isNotEqual() {
        Comment comment1 = Comment.builder().build();
        Comment comment2 = Comment.builder().build();

        AssertionsForClassTypes.assertThat(comment1).isNotEqualTo(comment2);
    }

    @Test
    @DisplayName("equals: 자기 자신과는 항상 동등하다")
    void equals_self_isEqual() {
        Comment comment = Comment.builder().build();
        ReflectionTestUtils.setField(comment, "id", 1L);

        AssertionsForClassTypes.assertThat(comment).isEqualTo(comment);
    }

    @Test
    @DisplayName("equals: 프록시 객체와 비교해도 ID가 같으면 동등하다")
    void equals_proxy() {
        Comment original = Comment.builder().build();
        ReflectionTestUtils.setField(original, "id", 100L);

        Comment proxy = new Comment() {
            @Override
            public Long getId() {
                return 100L;
            }
        };
        assertThat(original).isEqualTo(proxy);
    }
}
