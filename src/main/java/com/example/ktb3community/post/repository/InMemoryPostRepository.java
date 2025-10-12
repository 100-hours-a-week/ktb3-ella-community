package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPostRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();

    public Post save(Post post) {
        if (post.getId() == null) {
            long id = seq.getAndIncrement();
            post = Post.rehydrate(id, post.getUserId(), post.getTitle(), post.getContent(), post.getPostImageUrl(),
                    post.getLikeCount(), post.getViewCount(), post.getCommentCount(),
                    post.getCreatedAt(), post.getUpdatedAt(), post.getDeletedAt());
        }
        posts.put(post.getId(), post);
        return post;
    }

    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(posts.get(id))
                .filter(post -> post.getDeletedAt() == null);
    }

    public List<Post> findAll() {
        return posts.values().stream()
                .filter(p -> p.getDeletedAt() == null)
                .toList();
    }
}
