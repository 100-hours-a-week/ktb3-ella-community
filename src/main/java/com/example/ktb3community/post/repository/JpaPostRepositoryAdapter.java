package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.CursorPageRequest;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
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
    public List<Post> findAllByCursor(CursorPageRequest request) {
        PostSort sort = request.sort();
        NumberExpression<Long> sortPath = getSortPath(sort);

        return queryFactory
                .selectFrom(post)
                .join(post.user).fetchJoin()
                .where(
                        post.deletedAt.isNull(),
                        cursorCondition(request, sortPath)
                )
                .orderBy(
                        sortPath.desc(),
                        post.id.desc()
                )
                .limit(request.limit())
                .fetch();
    }

    private NumberExpression<Long> getSortPath(PostSort sort) {
        return switch (sort) {
            case VIEW -> post.viewCount;
            case LIKE -> post.likeCount;
            case CMT -> post.commentCount;
            case LATEST -> post.id;
        };
    }

    private BooleanExpression cursorCondition(CursorPageRequest request,
                                              NumberExpression<Long> sortPath) {
        Long cursorId = request.cursorId();
        Long cursorValue = request.cursorValue();
        PostSort sort = request.sort();

        // 첫 페이지
        if (cursorId == null) {
            return null;
        }

        // LATEST
        if (!sort.usesCursorValue()) {
            return post.id.lt(cursorId);
        }

        return sortPath.lt(cursorValue)
                .or(sortPath.eq(cursorValue).and(post.id.lt(cursorId)));
    }

    @Override
    public List<Post> findAllByIdIn(Collection<Long> ids) {
        return jpaPostRepository.findAllByIdInAndDeletedAtIsNull(ids);
    }
}
