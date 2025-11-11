package com.example.ktb3community.user.repository;

import com.example.ktb3community.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByNicknameAndDeletedAtIsNull(String nickname);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    List<User> findAllByIdInAndDeletedAtIsNull(Collection<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.deletedAt = :now, u.updatedAt = :now where u.id = :userId and u.deletedAt is null")
    int softDeleteById(@Param("userId") Long userId, @Param("now") Instant now);
}
