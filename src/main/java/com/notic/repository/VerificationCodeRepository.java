package com.notic.repository;

import com.notic.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    boolean existsByCode(long code);

    Optional<VerificationCode> findByCode(long code);

    void deleteAllByExpiresAtBefore(Instant now);
}
