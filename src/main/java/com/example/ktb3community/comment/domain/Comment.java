package com.example.ktb3community.comment.domain;

import com.example.ktb3community.common.domain.BaseTimeEntity;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Comment extends BaseTimeEntity {
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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static Comment createNew(Post post, User user, String content) {
        return Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .deletedAt(null)
                .build();
    }

    public void updateContent(String content) {
        if (content != null && !content.isBlank()) { this.content = content; }
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

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Comment other = (Comment) o;
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
