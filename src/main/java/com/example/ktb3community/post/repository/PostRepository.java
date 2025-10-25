package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(Long id);
    Post findByIdOrThrow(Long id);
    List<Post> findAll();
}
