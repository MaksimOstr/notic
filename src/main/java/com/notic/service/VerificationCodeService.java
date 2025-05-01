package com.notic.service;

import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.enums.VerificationCodeScopeEnum;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.VerificationCodeException;
import com.notic.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeService {
    private static final SecureRandom random = new SecureRandom();
    private static final int MIN_CODE = 10_000_000;
    private static final int MAX_CODE = 99_999_999;
    private static final int CODE_RANGE = MAX_CODE - MIN_CODE + 1;
    private static final int CODE_EXPIRY_MINUTES = 15;

    private final VerificationCodeRepository verificationCodeRepository;

    public VerificationCode create(User user, VerificationCodeScopeEnum scope) {
        VerificationCode verificationCode = new VerificationCode(
                user,
                generateUniqueCode(scope),
                getExpiryDate(),
                scope
        );

        try {
            return verificationCodeRepository.save(verificationCode);
        } catch (DataIntegrityViolationException e) {
            throw new EntityAlreadyExistsException("Failed to generate unique verification code due to conflict");
        }
    }

    @Transactional
    public long validate(int code, VerificationCodeScopeEnum scope) {
        VerificationCode verificationCode = findByCode(code, scope)
                .orElseThrow(() -> new VerificationCodeException("Invalid verification code"));
        Instant expiryAt = verificationCode.getExpiresAt();

        if(expiryAt.isBefore(Instant.now())) {
            deleteById(verificationCode.getId());
            throw new VerificationCodeException("Expired verification code");
        }

        long userId = verificationCode.getUser().getId();

        deleteById(verificationCode.getId());

        return userId;
    }


    private int generateUniqueCode(VerificationCodeScopeEnum scope) {
        for (int i = 0; i < 5; i++) {
            int code = generate8DigitCode();
            if (!verificationCodeRepository.existsByCodeAndScope(code, scope)) {
                return code;
            }
        }
        throw new EntityAlreadyExistsException("Failed to generate a unique verification code after 5 attempts");
    }

    private int generate8DigitCode() {
        return MIN_CODE + random.nextInt((CODE_RANGE));
    }

    private Instant getExpiryDate() {
        return Instant.now().plusSeconds(60 * CODE_EXPIRY_MINUTES);
    }

    private Optional<VerificationCode> findByCode(int code, VerificationCodeScopeEnum scope) {
        return verificationCodeRepository.findByCodeAndScope(code, scope);
    }

    private void deleteById(long id) {
        verificationCodeRepository.deleteById(id);
    }

    @Transactional
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void removeExpiredCodes() {
        Instant now = Instant.now();
        verificationCodeRepository.deleteAllByExpiresAtBefore(now);
    }
}
