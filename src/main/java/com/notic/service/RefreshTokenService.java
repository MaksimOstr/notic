package com.notic.service;

import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.TokenValidationException;
import com.notic.repository.RefreshTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static com.notic.utils.RefreshTokenUtils.*;


@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityManager entityManager;

    @Value("${REFRESH_TOKEN_SECRET}")
    private String refreshSecret;

    @Value("${REFRESH_TOKEN_TTL:3600}")
    private int refreshTokenTtl;

    @PostConstruct
    private void validateSecret() {
        if (refreshSecret == null || refreshSecret.length() < 32) {
            throw new IllegalStateException("REFRESH_TOKEN_SECRET must be at least 32 characters");
        }
    }

    @Transactional
    public String create(long userId) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUser_Id(userId);

        if (optionalRefreshToken.isPresent()) {
            return updateRefreshToken(optionalRefreshToken.get());
        } else {
            return createNewRefreshToken(userId);
        }
    }

    @Transactional
    public RefreshToken validate(String refreshToken) {
        RefreshToken token = findTokenByToken(refreshToken)
                .orElseThrow(() -> new TokenValidationException("Refresh token not found"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            deleteTokenById(token.getId());
            throw new TokenValidationException("Session is expired");
        }

        return token;
    }

    public String updateRefreshToken(RefreshToken token) {
        String newToken = generateToken();
        String hashed = hashedToken(newToken);

        token.setExpiresAt(getExpireTime());
        token.setToken(hashed);

        return newToken;
    }

    public void deleteTokenByToken(String token) {
        refreshTokenRepository.deleteByToken(hashedToken(token));
    }

    private String createNewRefreshToken(long userId) {
        try {
            String newToken = generateToken();
            RefreshToken refreshToken = new RefreshToken(
                    hashedToken(newToken),
                    entityManager.getReference(User.class, userId),
                    getExpireTime()
            );
            refreshTokenRepository.save(refreshToken);

            return newToken;
        } catch (DataIntegrityViolationException e) {
            throw new EntityDoesNotExistsException("User not found");
        }
    }

    public Optional<RefreshToken> findTokenByToken(String token) {
        return refreshTokenRepository.findByToken(hashedToken(token));
    }

    private void deleteTokenById(long id) {
        refreshTokenRepository.deleteById(id);
    }

    private Instant getExpireTime() {
        return Instant.now().plusSeconds(refreshTokenTtl);
    }

    private String hashedToken(String token) {
        return hashToken(token, refreshSecret);
    }

    @Transactional
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void removeExpiredTokens() {
        Instant now = Instant.now();
        refreshTokenRepository.deleteAllByExpiresAtBefore(now);
    }
}
