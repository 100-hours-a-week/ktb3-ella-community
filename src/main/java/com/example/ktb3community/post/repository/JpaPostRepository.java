package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    Page<Post> findByDeletedAtIsNull(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.deletedAt = :now where p.user.id = :userId and p.deletedAt is null")
    int softDeleteByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}
