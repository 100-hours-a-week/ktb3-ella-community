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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(nullable = false)
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

    private Post(Long id, User user, String title, String content, String postImageUrl, long like, long view, long cmt,
                 Instant deletedAt) {
        this.id = id;
        this.user = user;
        this.title = title.trim();
        this.content = content;
        this.postImageUrl = postImageUrl;
        this.likeCount = like;
        this.viewCount = view;
        this.commentCount = cmt;
        this.deletedAt = deletedAt;
    }

    public static Post createNew(User user, String title, String content, String postImageUrl) {
        return new Post(null, user, title, content, postImageUrl, 0, 0, 0, null);
    }
    public static Post rehydrate(Long id, User user, String title, String content, String postImageUrl, long like, long view, long cmt, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        Post post =  new Post(id, user, title, content, postImageUrl, like, view, cmt, deletedAt);
        post.createdAt = createdAt;
        post.updatedAt = updatedAt;
        return post;
    }

    public void updatePost(String title, String content, String postImageUrl) {
        if ( title != null && !title.isBlank() ){ this.title = title; }
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
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Post other = (Post) o;
        if(this.id == null || other.id == null ) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return System.identityHashCode(this);
    }
}
