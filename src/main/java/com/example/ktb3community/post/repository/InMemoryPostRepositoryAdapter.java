package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.exception.PostNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
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
            post = Post.rehydrate(id, post.getUser(), post.getTitle(), post.getContent(), post.getPostImageUrl(),
                    post.getLikeCount(), post.getViewCount(), post.getCommentCount(),
                    post.getCreatedAt(), post.getUpdatedAt(), post.getDeletedAt());
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
    public Page<Post> findAll(Pageable pageable) {
        List<Post> all = posts.values().stream()
                .filter(p -> p.getDeletedAt() == null)
                .sorted(resolveComparator(pageable.getSort()))
                .toList();

        int fromIndex = (int) Math.min(all.size(), pageable.getOffset());
        int toIndex = Math.min(all.size(), fromIndex + pageable.getPageSize());
        List<Post> content = fromIndex >= toIndex ? List.of() : all.subList(fromIndex, toIndex);

        return new PageImpl<>(content, pageable, all.size());
    }

    private Comparator<Post> resolveComparator(Sort sort) {
        Comparator<Post> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Post> propertyComparator = propertyComparator(order.getProperty());
            if (propertyComparator == null) {
                continue;
            }
            if (order.isDescending()) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = (comparator == null) ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator != null ? comparator : Comparator.comparing(Post::getId);
    }

    private Comparator<Post> propertyComparator(String property) {
        return POST_COMPARATORS.get(property);
    }

    private static final Map<String, Comparator<Post>> POST_COMPARATORS = Map.of(
            "createdAt", Comparator.comparing(Post::getCreatedAt),
            "viewCount", Comparator.comparingLong(Post::getViewCount),
            "likeCount", Comparator.comparingLong(Post::getLikeCount),
            "commentCount", Comparator.comparingLong(Post::getCommentCount),
            "id", Comparator.comparing(Post::getId)
    );
}
