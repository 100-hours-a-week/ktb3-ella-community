package com.example.ktb3community.user.repository;


import com.example.ktb3community.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository {
    // 시작값을 1로 초기화
    private final AtomicLong seq = new AtomicLong(1);
    // 키가 long, 값이 User인 해시맵
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();

    public User save(User user) {
        if (user.getId() == null) {
            long id = seq.getAndIncrement();
            user = User.rehydrate(id, user.getEmail(), user.getPasswordHash(), user.getNickname(),
                    user.getProfileImageUrl(), user.getCreatedAt(), user.getUpdatedAt(), user.getDeletedAt());
        }
        //키가 없으면 추가, 있으면 교체
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream().filter(u -> u.getEmail().equals(email))
                .filter(u -> u.getDeletedAt() == null).findAny();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public Optional<User> findByNickname(String nickname) {
        return users.values().stream()
                .filter(u -> u.getNickname().equals(nickname) )
                .filter(u -> u.getDeletedAt() == null).findAny();
    }

    public boolean existsByNickname(String nickname) {
        return findByNickname(nickname).isPresent();
    }

    public Optional<User> findById(Long id) {
        return users.values().stream().filter(u -> u.getId().equals(id))
                .filter(u -> u.getDeletedAt() == null).findAny();
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }
}
