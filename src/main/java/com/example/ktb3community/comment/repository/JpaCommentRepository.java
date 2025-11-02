package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findById(Long id);

    List<Comment> findByPost(Post post);
}
