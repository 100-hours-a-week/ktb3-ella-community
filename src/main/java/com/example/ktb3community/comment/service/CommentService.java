package com.example.ktb3community.comment.service;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.dto.CreateCommentRequest;
import com.example.ktb3community.comment.repository.InMemoryCommentRepository;
import com.example.ktb3community.post.dto.Author;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.example.ktb3community.post.repository.InMemoryPostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class CommentService {
    private final InMemoryUserRepository inMemoryUserRepository;
    private final InMemoryCommentRepository inMemoryCommentRepository;
    private final InMemoryPostRepository inMemoryPostRepository;

    public CommentResponse createComment(Long postId, CreateCommentRequest createCommentRequest) {
        User user = inMemoryUserRepository.findById(createCommentRequest.userId())
                .orElseThrow(UserNotFoundException::new);
        if(!inMemoryPostRepository.existsById(postId)) {
            throw new PostNotFoundException();
        }
        Comment saved = inMemoryCommentRepository.save(Comment.createNew(postId, createCommentRequest.userId(),
                createCommentRequest.content(), Instant.now()));
        Author author = new Author(user.getNickname(), user.getProfileImageUrl());
        return new CommentResponse(saved.getId(), saved.getContent(), author, saved.getCreatedAt());
    }

}
