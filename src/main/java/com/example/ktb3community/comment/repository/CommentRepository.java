package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);
    Optional<Comment> findById(Long id);
    Comment findByIdOrThrow(Long id);
    List<Comment> findByPostId(Long postId);
}
