package com.example.ktb3community.post.domain;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Post {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String postImageUrl;
    private long likeCount;
    private long viewCount;
    private long commentCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Post(Long id, Long userId, String title, String content, String postImageUrl, long like, long view, long cmt,
                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title.trim();
        this.content = content;
        this.postImageUrl = postImageUrl;
        this.likeCount = like;
        this.viewCount = view;
        this.commentCount = cmt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Post createNew(Long userId, String title, String content, String postImageUrl, Instant now) {
        return new Post(null, userId, title, content, postImageUrl, 0, 0, 0, now, now, null);
    }
    public static Post rehydrate(Long id, Long userId, String title, String content, String postImageUrl, long like, long view, long cmt,
                                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Post(id, userId, title, content, postImageUrl, like, view, cmt, createdAt, updatedAt, deletedAt);
    }

    public void updatePost(String title, String content, String postImageUrl, Instant now) {
        this.title = title;
        this.content = content;
        this.postImageUrl = postImageUrl;
        this.updatedAt = now;
    }

    public void increaseView() {
        this.viewCount++;
    }
    public void increaseLike() {
        this.likeCount++;
    }
    public void decreaseLike() {
        if (this.likeCount > 0) this.likeCount--;
    }
    public void increaseComment() {
        this.commentCount++;
    }
    public void decreaseComment() {
        if (this.commentCount > 0) this.commentCount--;
    }
    public void delete(Instant now) {
        if (this.deletedAt == null) { this.deletedAt = now; this.updatedAt = now; }
    }
}

