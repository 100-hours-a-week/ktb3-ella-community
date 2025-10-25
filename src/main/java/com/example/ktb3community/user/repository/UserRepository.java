package com.example.ktb3community.user.repository;

import com.example.ktb3community.user.domain.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
    Optional<User> findById(Long id);
    User findByIdOrThrow(Long id);
    List<User> findAllByIdIn(Collection<Long> ids);
}
