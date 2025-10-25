package com.example.ktb3community.post.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InMemoryPostLikeRepository implements PostLikeRepository {
    private final Map<Long, Set<Long>> userPost = new ConcurrentHashMap<>();

    @Override
    public boolean exists(long postId, long userId) {
        Set<Long> set = userPost.get(postId);
        return set != null && set.contains(userId);
    }
    @Override
    public boolean add(long postId, long userId) {
        return userPost
                .computeIfAbsent(postId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);
    }
    @Override
    public boolean remove(long postId, long userId) {
        Set<Long> set = userPost.get(postId);
        return set != null && set.remove(userId);
    }
}
