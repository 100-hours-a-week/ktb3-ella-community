package com.example.ktb3community.post.service;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.*;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class PostService implements PostCommentCounter {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        userRepository.findByIdOrThrow(userId);
        Post saved = postRepository.save(Post.createNew(userId, createPostRequest.title(),
                createPostRequest.content(), createPostRequest.postImageUrl(), Instant.now()));
        return new CreatePostResponse(saved.getId());
    }

    public CreatePostResponse updatePost(Long postId, Long userId, CreatePostRequest createPostRequest) {
        userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(postId);
        if(!post.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        post.updatePost(createPostRequest.title(), createPostRequest.content(), createPostRequest.postImageUrl(), Instant.now());
        return new CreatePostResponse(post.getId());
    }

    public void deletePost(Long postId, Long userId) {
        userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(postId);
        if(!post.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        post.delete(Instant.now());
    }

    @Override
    public void increaseCommentCount(Long postId) {
        Post post = postRepository.findByIdOrThrow(postId);
        post.increaseCommentCount();
        postRepository.save(post);
    }

    @Override
    public void decreaseCommentCount(Long postId) {
        Post post = postRepository.findByIdOrThrow(postId);
        post.decreaseCommentCount();
        postRepository.save(post);
    }
}
