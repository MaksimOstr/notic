package com.notic.repository;

import com.notic.entity.VerificationCode;
import com.notic.enums.VerificationCodeScopeEnum;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    boolean existsByCodeAndScope(int code, VerificationCodeScopeEnum scope);

    Optional<VerificationCode> findByCodeAndScope(int code, VerificationCodeScopeEnum scope);

    void deleteAllByExpiresAtBefore(Instant now);

    void deleteById(long id);
}
