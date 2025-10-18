package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.exception.CommentNotFound;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCommentRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Comment> comments = new ConcurrentHashMap<>();

    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            long id = seq.getAndIncrement();
            comment = Comment.rehydrate(id, comment.getPostId(), comment.getUserId(), comment.getContent(),
                    comment.getCreatedAt(), comment.getUpdatedAt(), comment.getDeletedAt());
        }
        comments.put(comment.getId(), comment);
        return comment;
    }
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(comments.get(id))
                .filter(comment -> comment.getDeletedAt() == null);
    }

    public Comment findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(CommentNotFound::new);
    }

    public List<Comment> findByPostId(Long postId) {
        return comments.values().stream()
                .filter(p -> p.getDeletedAt() == null && p.getPostId().equals(postId))
                .toList();
    }
}
