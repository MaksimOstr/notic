package com.notic.repository;

import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsByToken(String token);

    void deleteAllByUser(User user);
}
