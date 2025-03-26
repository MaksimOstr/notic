package com.notic.repository;

import com.notic.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    boolean existsByCode(long code);

    Optional<VerificationCode> findByCode(long code);
}
