package com.example.ktb3community.user.repository;


import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepositoryAdapter implements UserRepository {
    // 시작값을 1로 초기화
    private final AtomicLong seq = new AtomicLong(1);
    // 키가 long, 값이 User인 해시맵
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Long> emailToUserId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> nicknameToUserId = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        User userToSave = user;
        if (userToSave.getId() == null) {
            long id = seq.getAndIncrement();
            userToSave = User.rehydrate(id, user.getEmail(), user.getPasswordHash(), user.getNickname(),
                    user.getProfileImageUrl(), user.getCreatedAt(), user.getUpdatedAt(), user.getDeletedAt());
            emailToUserId.put(userToSave.getEmail(), id);
            nicknameToUserId.put(userToSave.getNickname(), id);
        } else {
            User oldUser = users.get(userToSave.getId());
            if (oldUser != null && !oldUser.getNickname().equals(userToSave.getNickname())) {
                nicknameToUserId.remove(oldUser.getNickname());
                nicknameToUserId.put(userToSave.getNickname(), userToSave.getId());
            }
        }
        users.put(userToSave.getId(), userToSave);
        return userToSave;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Long userId = emailToUserId.get(email);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(userId))
                .filter(u -> u.getDeletedAt() == null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        Long userId = nicknameToUserId.get(nickname);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(userId))
                .filter(u -> u.getDeletedAt() == null);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return findByNickname(nickname).isPresent();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id))
                .filter(u -> u.getDeletedAt() == null);
    }

    @Override
    public User findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<User> findAllByIdIn(Collection<Long> ids) {
        return ids.stream()
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
