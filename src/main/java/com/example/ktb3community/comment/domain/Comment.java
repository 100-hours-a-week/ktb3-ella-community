package com.example.ktb3community.comment.domain;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Comment(Long id, Long postId, Long userId, String content, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }
    public static Comment createNew(Long postId, Long userId, String content, Instant now) {
        return new Comment(null, postId, userId, content, now, now, null);
    }
    public static Comment rehydrate(Long id, Long postId, Long userId, String content, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Comment(id, postId, userId, content, createdAt, updatedAt, deletedAt);
    }
    public void updateContent(String content, Instant now) {
        if (content == null) { this.content = content; this.updatedAt = now; }
    }
    public void delete(Instant now) {
        if(this.deletedAt == null ) { deletedAt = now; }
    }
}

