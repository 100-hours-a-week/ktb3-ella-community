package com.example.ktb3community.user.repository;

import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
        return jpaUserRepository.findByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return jpaUserRepository.findByNicknameAndDeletedAtIsNull(nickname);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaUserRepository.existsByNicknameAndDeletedAtIsNull(nickname);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public User findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<User> findAllByIdIn(Collection<Long> ids) {
        return jpaUserRepository.findAllByIdInAndDeletedAtIsNull(ids);
    }

    @Override
    public void softDeleteById(Long id, Instant now) {
        jpaUserRepository.softDeleteById(id, now);
    }
}
