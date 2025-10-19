package com.example.ktb3community.post.service;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.*;
import com.example.ktb3community.post.repository.InMemoryPostRepository;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class PostService implements PostCommentCounter {
    private final InMemoryPostRepository inMemoryPostRepository;
    private final InMemoryUserRepository inMemoryUserRepository;

    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        inMemoryUserRepository.findByIdOrThrow(userId);
        Post saved = inMemoryPostRepository.save(Post.createNew(userId, createPostRequest.title(),
                createPostRequest.content(), createPostRequest.postImageUrl(), Instant.now()));
        return new CreatePostResponse(saved.getId());
    }

    public CreatePostResponse updatePost(Long postId, Long userId, CreatePostRequest createPostRequest) {
        inMemoryUserRepository.findByIdOrThrow(userId);
        Post post = inMemoryPostRepository.findByIdOrThrow(postId);
        if(!post.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        post.updatePost(createPostRequest.title(), createPostRequest.content(), createPostRequest.postImageUrl(), Instant.now());
        return new CreatePostResponse(post.getId());
    }

    public void deletePost(Long postId, Long userId) {
        inMemoryUserRepository.findByIdOrThrow(userId);
        Post post = inMemoryPostRepository.findByIdOrThrow(postId);
        if(!post.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        post.delete(Instant.now());
    }

    public void increaseCommentCount(Long postId) {
        Post post = inMemoryPostRepository.findByIdOrThrow(postId);
        post.increaseCommentCount();
        inMemoryPostRepository.save(post);
    }

    public void decreaseCommentCount(Long postId) {
        Post post = inMemoryPostRepository.findByIdOrThrow(postId);
        post.decreaseCommentCount();
        inMemoryPostRepository.save(post);
    }
}
