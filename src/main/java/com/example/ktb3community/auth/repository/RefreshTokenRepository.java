package com.example.ktb3community.auth.repository;

import com.example.ktb3community.auth.domain.RefreshToken;
import com.example.ktb3community.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken rt set rt.revoked = true " +
            "where rt.user = :user and rt.revoked = false")
    int revokeAllByUser(User user);
}
