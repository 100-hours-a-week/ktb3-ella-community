package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.example.ktb3community.post.domain.QPost.post;

@Repository
@AllArgsConstructor
@Primary
public class JpaPostRepositoryAdapter implements PostRepository {

    private final JpaPostRepository jpaPostRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Post save(Post post) {
        return jpaPostRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jpaPostRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Post findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Override
    public int softDeleteByUserId(Long userId, Instant now) {
        return jpaPostRepository.softDeleteByUserId(userId, now);
    }

    @Override
    public List<Post> findAllByCursor(Long cursorId, Long cursorValue, PostSort sort, Pageable pageable) {

        NumberExpression<Long> sortPath = getSortPath(sort);

        return queryFactory
                .selectFrom(post)
                .join(post.user).fetchJoin()
                .where(
                        post.deletedAt.isNull(),
                        cursorCondition(cursorId, cursorValue, sortPath)
                )
                .orderBy(
                        sortPath.desc(),
                        post.id.desc()
                )
                .limit(pageable.getPageSize())
                .fetch();
    }

    private NumberExpression<Long> getSortPath(PostSort sort) {
        return switch (sort) {
            case VIEW -> post.viewCount;
            case LIKE -> post.likeCount;
            case CMT -> post.commentCount;
            default -> post.id;
        };
    }

    private BooleanExpression cursorCondition(Long cursorId, Long cursorValue, NumberExpression<Long> path) {

        if (cursorId == null) return null; // 첫 페이지

        // 최신순일 경우: ID만 비교
        if (path.equals(post.id)) {
            return post.id.lt(cursorId);
        }

        // 나머지 경우
        return path.lt(cursorValue)
                .or(path.eq(cursorValue).and(post.id.lt(cursorId)));
    }
}
