package com.example.ktb3community;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.common.Role;
import com.example.ktb3community.post.domain.Like;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.user.domain.User;

import java.time.Instant;

import static com.example.ktb3community.TestFixtures.COMMENT_ID;
import static com.example.ktb3community.TestFixtures.POST_ID;
import static com.example.ktb3community.TestFixtures.TOKEN_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;

public final class TestEntityFactory {

    private static final String DEFAULT_EMAIL = "user@test.com";
    private static final String DEFAULT_PASSWORD_HASH = "password-hash";
    private static final String DEFAULT_NICKNAME = "tester";
    private static final String DEFAULT_PROFILE_IMAGE = "http://image.test/profile.jpg";

    private static final String DEFAULT_POST_TITLE = "Post Title";
    private static final String DEFAULT_POST_CONTENT = "Post Content";
    private static final String DEFAULT_POST_IMAGE_URL = "http://image.test/post.jpg";

    private static final String DEFAULT_COMMENT_CONTENT = "Comment Content";

    private static final Instant DEFAULT_REFRESH_EXPIRES_AT = Instant.parse("2099-01-01T00:00:00Z");

    private TestEntityFactory() {
    }

    public static User.UserBuilder user() {
        return User.builder()
                .id(USER_ID)
                .email(DEFAULT_EMAIL)
                .passwordHash(DEFAULT_PASSWORD_HASH)
                .nickname(DEFAULT_NICKNAME)
                .profileImageUrl(DEFAULT_PROFILE_IMAGE)
                .role(Role.ROLE_USER);
    }

    public static Post.PostBuilder post() {
        return post(user().build());
    }

    public static Post.PostBuilder post(User user) {
        return Post.builder()
                .id(POST_ID)
                .user(user)
                .title(DEFAULT_POST_TITLE)
                .content(DEFAULT_POST_CONTENT)
                .postImageUrl(DEFAULT_POST_IMAGE_URL)
                .likeCount(0)
                .viewCount(0)
                .commentCount(0)
                .deletedAt(null);
    }

    public static Comment.CommentBuilder comment(Post post, User user) {
        return Comment.builder()
                .id(COMMENT_ID)
                .post(post)
                .user(user)
                .content(DEFAULT_COMMENT_CONTENT)
                .deletedAt(null);
    }

    public static Like.LikeBuilder like(Post post, User user) {
        return Like.builder()
                .post(post)
                .user(user)
                .deletedAt(null);
    }

    public static RefreshToken.RefreshTokenBuilder refreshToken(User user) {
        return RefreshToken.builder()
                .id(TOKEN_ID)
                .user(user)
                .expiresAt(DEFAULT_REFRESH_EXPIRES_AT)
                .revoked(false)
                .version(0L);
    }
}
