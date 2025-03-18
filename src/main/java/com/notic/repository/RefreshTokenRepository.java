package com.notic.repository;

import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsByToken(String token);

    void deleteAllByUser(User user);

    void deleteByToken(String token);

    @EntityGraph(value = "RefreshToken.withUserAndRoles", type = EntityGraph.EntityGraphType.FETCH)
    Optional<RefreshToken> findByToken(String token);
}
