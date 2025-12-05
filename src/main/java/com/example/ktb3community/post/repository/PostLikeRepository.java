package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.user.domain.User;

public interface PostLikeRepository {
    boolean exists(long postId, long userId);

    boolean add(Post post, User user);

    boolean remove(Post post, User user);
}
