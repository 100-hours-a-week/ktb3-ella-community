package com.example.ktb3community.comment.repository;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findByPostAndDeletedAtIsNull(Post post, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Comment c set c.deletedAt = :now where c.user.id = :userId and c.deletedAt is null")
    int softDeleteByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("update Comment c set c.deletedAt = :now, c.updatedAt =:now  where c.post.id = :postId and c.deletedAt is null")
    int softDeleteByPostId(@Param("postId") Long postId, @Param("now") Instant now);
}
