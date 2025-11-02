package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.exception.CommentNotFound;
import com.example.ktb3community.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCommentRepositoryAdapter implements CommentRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Comment> comments = new ConcurrentHashMap<>();

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            long id = seq.getAndIncrement();
            comment = Comment.rehydrate(id, comment.getPost(), comment.getUser(), comment.getContent(),
                    comment.getCreatedAt(), comment.getUpdatedAt(), comment.getDeletedAt());
        }
        comments.put(comment.getId(), comment);
        return comment;
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(comments.get(id))
                .filter(comment -> comment.getDeletedAt() == null);
    }

    @Override
    public Comment findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(CommentNotFound::new);
    }

    @Override
    public Page<Comment> findByPost(Post post, Pageable pageable) {
        Long postId = post.getId();
        List<Comment> all = comments.values().stream()
                .filter(comment -> comment.getDeletedAt() == null && comment.getPostId().equals(postId))
                .sorted(resolveComparator(pageable.getSort()))
                .toList();

        int fromIndex = (int) Math.min(all.size(), pageable.getOffset());
        int toIndex = Math.min(all.size(), fromIndex + pageable.getPageSize());
        List<Comment> content = fromIndex >= toIndex ? List.of() : all.subList(fromIndex, toIndex);

        return new PageImpl<>(content, pageable, all.size());
    }

    private Comparator<Comment> resolveComparator(Sort sort) {
        Comparator<Comment> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Comment> propertyComparator = propertyComparator(order.getProperty());
            if (propertyComparator == null) {
                continue;
            }
            if (order.isDescending()) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = (comparator == null) ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator != null ? comparator : Comparator.comparing(Comment::getId);
    }

    private Comparator<Comment> propertyComparator(String property) {
        return COMMENT_COMPARATORS.get(property);
    }

    private static final Map<String, Comparator<Comment>> COMMENT_COMPARATORS = Map.of(
            "createdAt", Comparator.comparing(Comment::getCreatedAt),
            "id", Comparator.comparing(Comment::getId)
    );
}
