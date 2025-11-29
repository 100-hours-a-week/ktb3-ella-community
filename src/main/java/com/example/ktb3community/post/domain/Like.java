package com.example.ktb3community.post.domain;

import com.example.ktb3community.common.domain.BaseTimeEntity;
import com.example.ktb3community.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_like_post_user", columnNames = {"post_id", "user_id"})
})
public class Like extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "deleted_at")
    private Instant deletedAt;


    public static Like createNew(Post post, User user) {
        return Like.builder()
                .post(post)
                .user(user)
                .deletedAt(null)
                .build();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete(Instant now) {
        if (deletedAt == null) {
            this.deletedAt = now;
        }
    }

    // 좋아요 복구
    public void restore() {
        if (deletedAt != null) {
            this.deletedAt = null;
        }
    }
}
