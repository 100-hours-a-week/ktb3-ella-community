package com.example.ktb3community.comment.domain;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private Comment(Long id, Post post, User user, String content, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Comment createNew(Post post, User user, String content, Instant now) {
        return new Comment(null, post, user, content, now, now, null);
    }

    public static Comment rehydrate(Long id, Post post, User user, String content, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Comment(id, post, user, content, createdAt, updatedAt, deletedAt);
    }

    public void updateContent(String content, Instant now) {
        if (content != null && !content.isBlank()) { this.content = content; this.updatedAt = now; }
    }

    public void delete(Instant now) {
        if(this.deletedAt == null ) {
            deletedAt = now;
        }
    }

    public Long getUserId() {
        if(user == null) {
            throw new UserNotFoundException();
        }
        return user.getId();
    }

    public Long getPostId() {
        if(post == null) {
            throw new PostNotFoundException();
        }
        return post.getId();
    }
}

