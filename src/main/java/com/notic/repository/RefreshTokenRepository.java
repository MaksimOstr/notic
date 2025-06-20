package com.notic.repository;

import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser_Id(Long id);

    @Modifying
    @Transactional
    void deleteByToken(String token);

    @EntityGraph(value = "RefreshToken.withUserAndRoles", type = EntityGraph.EntityGraphType.FETCH)
    Optional<RefreshToken> findByToken(String token);

    void deleteAllByExpiresAtBefore(Instant expiresAtBefore);

    List<RefreshToken> findByUser_(User user);
}
