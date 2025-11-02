package com.example.ktb3community.post.repository;

import com.example.ktb3community.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long id);
}
