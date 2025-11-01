package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.exception.PostNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPostRepository implements PostRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();

    @Override
    public Post save(Post post) {
        if (post.getId() == null) {
            long id = seq.getAndIncrement();
            post = Post.rehydrate(id, post.getUser(), post.getTitle(), post.getContent(), post.getPostImageUrl(),
                    post.getLikeCount(), post.getViewCount(), post.getCommentCount(),
                    post.getCreatedAt(), post.getUpdatedAt(), post.getDeletedAt());
        }
        posts.put(post.getId(), post);
        return post;
    }
    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(posts.get(id))
                .filter(post -> post.getDeletedAt() == null);
    }
    @Override
    public Post findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(PostNotFoundException::new);
    }
    @Override
    public List<Post> findAll() {
        return posts.values().stream()
                .filter(p -> p.getDeletedAt() == null)
                .toList();
    }
}
