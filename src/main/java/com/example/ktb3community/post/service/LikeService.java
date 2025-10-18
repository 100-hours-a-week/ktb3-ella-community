package com.example.ktb3community.post.service;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.LikeResponse;
import com.example.ktb3community.post.repository.InMemoryPostLikeRepository;
import com.example.ktb3community.post.repository.InMemoryPostRepository;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LikeService {
    private final InMemoryUserRepository inMemoryUserRepository;
    private final InMemoryPostRepository inMemoryPostRepository;
    private final InMemoryPostLikeRepository inMemoryPostLikeRepository;

    public LikeResponse likePost(Long postId, Long userId){
        inMemoryUserRepository.findByIdOrThrow(userId);
        Post post = inMemoryPostRepository.findByIdOrThrow(postId);
        boolean added = inMemoryPostLikeRepository.add(postId, userId);
        if (added) post.increaseLike();
        return new LikeResponse(post.getLikeCount(), post.getViewCount(), post.getCommentCount());
    }

    public LikeResponse unlikePost(Long postId, Long userId){
        inMemoryUserRepository.findByIdOrThrow(userId);
        Post post = inMemoryPostRepository.findByIdOrThrow(postId);
        boolean removed = inMemoryPostLikeRepository.remove(postId, userId);
        if (removed) post.decreaseLike();
        return new LikeResponse(post.getLikeCount(), post.getViewCount(), post.getCommentCount());
    }
}
