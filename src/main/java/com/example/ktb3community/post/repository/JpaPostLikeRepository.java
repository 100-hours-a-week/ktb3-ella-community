package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Like;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPostLikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPost_IdAndUser_IdAndDeletedAtIsNull(Long postId, Long userId);

    Optional<Like> findByPostAndUser(Post post, User user);

}
