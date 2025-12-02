package com.example.ktb3community.post.repository.inmemory;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.repository.PostLikeRepository;
import com.example.ktb3community.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPostLikeRepositoryAdapter implements PostLikeRepository {
    private final Map<Long, Set<Long>> likesByPostId = new ConcurrentHashMap<>();

    @Override
    public boolean exists(Post post, User user) {
        Set<Long> userIds = likesByPostId.get(post.getId());
        Long userId = user.getId();
        return userIds != null && userId != null && userIds.contains(userId);
    }

    @Override
    public boolean add(Post post, User user) {
        Long postId = post.getId();
        Long userId = user.getId();
        if (postId == null || userId == null) {
            return false;
        }
        return likesByPostId
                .computeIfAbsent(postId, key -> ConcurrentHashMap.newKeySet())
                .add(userId);
    }

    @Override
    public boolean remove(Post post, User user) {
        Long postId = post.getId();
        Long userId = user.getId();
        if (postId == null || userId == null) {
            return false;
        }
        Set<Long> userIds = likesByPostId.get(postId);
        if (userIds == null) {
            return false;
        }
        boolean removed = userIds.remove(userId);
        if (removed && userIds.isEmpty()) {
            likesByPostId.remove(postId);
        }
        return removed;
    }
}
