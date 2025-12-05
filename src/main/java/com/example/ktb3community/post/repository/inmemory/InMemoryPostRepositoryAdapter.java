package com.example.ktb3community.post.repository.inmemory;

import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.CursorPageRequest;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.example.ktb3community.post.repository.PostRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPostRepositoryAdapter implements PostRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();

    @Override
    public Post save(Post post) {
        if (post.getId() == null) {
            long id = seq.getAndIncrement();
            post = Post.builder()
                    .id(id)
                    .user(post.getUser())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .postImageUrl(post.getPostImageUrl())
                    .likeCount(post.getLikeCount())
                    .viewCount(post.getViewCount())
                    .commentCount(post.getCommentCount())
                    .deletedAt(null)
                    .build();
        }
        posts.put(post.getId(), post);
        return post;
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(posts.get(id))
                .filter(post -> post.getDeletedAt() == null);
    }

    @Override
    public Post findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(PostNotFoundException::new);
    }

    @Override
    public int softDeleteByUserId(Long userId, Instant now) {
        return posts.values().stream()
                .filter(post -> post.getDeletedAt() == null && post.getUserId().equals(userId))
                .mapToInt(post -> {
                    post.delete(now);
                    return 1;
                })
                .sum();
    }

    @Override
    public List<Post> findAllByCursor(CursorPageRequest request) {
        PostSort sort = request.sort();
        Comparator<Post> comparator = sort.descendingComparator();

        return posts.values().stream()
                .filter(post -> post.getDeletedAt() == null)
                .filter(post -> isAfterCursor(post, request))
                .sorted(comparator)
                .limit(request.limit())
                .toList();
    }

    private boolean isAfterCursor(Post post, CursorPageRequest request) {
        Long cursorId = request.cursorId();
        Long cursorValue = request.cursorValue();
        PostSort sort = request.sort();

        // 첫 페이지
        if (cursorId == null) {
            return true;
        }

        // LATEST처럼 cursorValue를 쓰지 않는 경우
        if (!sort.usesCursorValue()) {
            return post.getId() < cursorId;
        }

        long key = sort.extractKey(post);
        if (key < cursorValue) return true;
        if (key > cursorValue) return false;
        return post.getId() < cursorId;
    }

    @Override
    public List<Post> findAllByIdIn(Collection<Long> ids) {
        return ids.stream()
                .map(posts::get)
                .filter(post -> post != null && post.getDeletedAt() == null)
                .toList();
    }
}
