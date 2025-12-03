package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(Long id);

    Post findByIdOrThrow(Long id);

    int softDeleteByUserId(Long userId, Instant now);

    List<Post> findAllByCursor(Long cursorId, Long cursorValue, PostSort sort, Pageable pageable);
}
