package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.exception.CommentNotFound;
import com.example.ktb3community.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        return jpaCommentRepository.findById(id);
    }

    @Override
    public Comment findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(CommentNotFound::new);
    }

    @Override
    public List<Comment> findByPost(Post post) {
        return jpaCommentRepository.findByPost(post);
    }
}
