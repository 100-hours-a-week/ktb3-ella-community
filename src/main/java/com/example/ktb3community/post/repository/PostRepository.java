package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(Long id);

    Post findByIdOrThrow(Long id);

    Page<Post> findAll(Pageable pageable);
}
