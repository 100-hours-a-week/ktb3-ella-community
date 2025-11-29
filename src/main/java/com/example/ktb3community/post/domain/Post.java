package com.example.ktb3community.post.domain;

import com.example.ktb3community.common.domain.BaseTimeEntity;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "post_image_url")
    private String postImageUrl;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static Post createNew(User user, String title, String content, String postImageUrl) {
        return Post.builder()
                .user(user)
                .title(title.trim())
                .content(content)
                .postImageUrl(postImageUrl)
                .likeCount(0)
                .viewCount(0)
                .commentCount(0)
                .deletedAt(null)
                .build();
    }

    public void updatePost(String title, String content, String postImageUrl) {
        if ( title != null && !title.isBlank() ){ this.title = title.trim(); }
        if ( content != null && !content.isBlank() ) { this.content = content; }
        this.postImageUrl = postImageUrl;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0 ) { this.likeCount--; }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) this.commentCount--;
    }

    public void delete(Instant now) {
        if (this.deletedAt == null) {
            this.deletedAt = now;
        }
    }

    public Long getUserId() {
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post post)) return false;

        return id != null && id.equals(post.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
