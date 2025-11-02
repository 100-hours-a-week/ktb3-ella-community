package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Like;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
@Primary
@RequiredArgsConstructor
public class JpaPostLikeRepositoryAdapter implements PostLikeRepository {

    private final JpaPostLikeRepository jpaPostLikeRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Post post, User user) {
        return jpaPostLikeRepository.existsByPostAndUserAndDeletedAtIsNull(post, user);
    }

    @Override
    @Transactional
    public boolean add(Post post, User user) {
        Instant now = Instant.now();
        return jpaPostLikeRepository.findByPostAndUser(post, user)
                .map(existing -> {
                    if (!existing.isDeleted()) {
                        return false;
                    }
                    existing.restore(now);
                    return true;
                })
                .orElseGet(() -> {
                    Like like = Like.createNew(post, user, now);
                    jpaPostLikeRepository.save(like);
                    return true;
                });
    }

    @Override
    @Transactional
    public boolean remove(Post post, User user) {
        return jpaPostLikeRepository.findByPostAndUser(post, user)
                .filter(like -> !like.isDeleted())
                .map(like -> {
                    like.delete(Instant.now());
                    return true;
                })
                .orElse(false);
    }
}
