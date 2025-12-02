package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.exception.PostNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Primary
public class JpaPostRepositoryAdapter implements PostRepository {

    private final JpaPostRepository jpaPostRepository;

    @Override
    public Post save(Post post) {
        return jpaPostRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jpaPostRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Post findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return jpaPostRepository.findByDeletedAtIsNull(pageable);
    }

    @Override
    public int softDeleteByUserId(Long userId, Instant now) {
        return jpaPostRepository.softDeleteByUserId(userId, now);
    }

    @Override
    public List<Post> findAllByCursorWithUser(Long cursorId, Pageable pageable) {
        if (cursorId == null) {
            return jpaPostRepository.findAllByOrderByIdDesc(pageable);
        } else {
            return jpaPostRepository.findByIdLessThanOrderByIdDesc(cursorId, pageable);
        }
    }
}
