package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    Comment findByIdOrThrow(Long id);


    Page<Comment> findByPost(Post post, Pageable pageable);
}
