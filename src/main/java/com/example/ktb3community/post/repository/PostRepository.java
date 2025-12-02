package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(Long id);

    Post findByIdOrThrow(Long id);

    Page<Post> findAll(Pageable pageable);

    Page<Post> findAllWithAuthor(Pageable pageable);

    int softDeleteByUserId(Long userId, Instant now);

    //  성능 테스트 및 배치 처리를 위해 추가 ---
    void saveAll(List<Post> posts);
    long count();
    // ----------------------------------
}
