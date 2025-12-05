package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.CursorPageRequest;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(Long id);

    Post findByIdOrThrow(Long id);

    int softDeleteByUserId(Long userId, Instant now);

    List<Post> findAllByCursor(CursorPageRequest request);

    List<Post> findAllByIdIn(Collection<Long> ids);
}
