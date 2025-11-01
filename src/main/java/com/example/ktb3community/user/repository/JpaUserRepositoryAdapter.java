package com.example.ktb3community.user.repository;

import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return jpaUserRepository.findByNickname(nickname);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaUserRepository.existsByNickname(nickname);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public User findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<User> findAllByIdIn(Collection<Long> ids) {
        return jpaUserRepository.findAllByIdIn(ids);
    }
}
