package com.example.ktb3community.post.service;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.LikeResponse;
import com.example.ktb3community.post.repository.PostLikeRepository;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LikeService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public LikeResponse likePost(Long postId, Long userId){
        User user = userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(postId);
        boolean added = postLikeRepository.add(post, user);
        if (added) {
            post.increaseLikeCount();
            postRepository.save(post);
        }
        return new LikeResponse(post.getLikeCount(), post.getViewCount(), post.getCommentCount());
    }

    @Transactional
    public LikeResponse unlikePost(Long postId, Long userId){
        User user = userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(postId);
        boolean removed = postLikeRepository.remove(post, user);
        if (removed) {
            post.decreaseLikeCount();
            postRepository.save(post);
        }
        return new LikeResponse(post.getLikeCount(), post.getViewCount(), post.getCommentCount());
    }
}
