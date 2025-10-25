package com.example.ktb3community.post.repository;

public interface PostLikeRepository {
    boolean exists(long postId, long userId);
    boolean add(long postId, long userId);
    boolean remove(long postId, long userId);

}
