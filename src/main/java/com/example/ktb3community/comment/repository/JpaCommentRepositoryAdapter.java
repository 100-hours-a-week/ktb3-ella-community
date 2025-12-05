package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.exception.CommentNotFound;
import com.example.ktb3community.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class JpaCommentRepositoryAdapter implements CommentRepository {

    private final JpaCommentRepository jpaCommentRepository;

    @Override
    public Comment save(Comment comment) {
        return jpaCommentRepository.save(comment);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return jpaCommentRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Comment findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(CommentNotFound::new);
    }

    @Override
    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        return jpaCommentRepository.findByPost_IdAndDeletedAtIsNull(postId, pageable);
    }

    @Override
    public int softDeleteByUserId(Long userId, Instant now) {
        return jpaCommentRepository.softDeleteByUserId(userId, now);
    }

    @Override
    public void softDeleteByPostId(Long postId, Instant now) {
        jpaCommentRepository.softDeleteByPostId(postId, now);
    }
}
