package com.notic.service;

import com.notic.constants.TokenConstants;
import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.TokenValidationException;
import com.notic.repository.RefreshTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${REFRESH_TOKEN_SECRET:1212}")
    private String refreshSecret;

    @Value("${REFRESH_TOKEN_TTL:3600}")
    private int refreshTokenTtl;

    @PostConstruct
    private void validateSecret() {
        if (refreshSecret == null || refreshSecret.length() < 32) {
            throw new IllegalStateException("REFRESH_TOKEN_SECRET must be at least 32 characters");
        }
    }

    @Transactional(rollbackFor = EntityAlreadyExistsException.class)
    public String getRefreshToken(User user, boolean isValidation) {
        String token = generateToken();
        String hashToken = hashToken(token);

        RefreshToken refreshToken = new RefreshToken(
                hashToken,
                user,
                getExpireTime()
        );

        if(!isValidation) {
            deleteAllTokensByUser(user);
        }

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public Cookie getRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        cookie.setHttpOnly(true);
        cookie.setMaxAge(refreshTokenTtl);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }

    @Transactional(rollbackFor = {EntityDoesNotExistsException.class, TokenValidationException.class})
    public RefreshToken validateToken(String refreshToken) {
        RefreshToken token = findTokenByToken(hashToken(refreshToken));

        if(token.getExpiresAt().isBefore(Instant.now())) {
            deleteTokenByToken(token.getToken());
            throw new TokenValidationException("Session is expired");
        }

        deleteTokenByToken(refreshToken);
        return token;
    }

    private String hashToken(String token) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] hashedBytes = sha256HMAC.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    private String generateToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        return UUID.randomUUID() + HexFormat.of().formatHex(randomBytes);
    }

    private RefreshToken findTokenByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenValidationException("Session is expired"));
    }

    private void deleteAllTokensByUser(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    private void deleteTokenByToken(String token) {
        String hashedToken = hashToken(token);
        refreshTokenRepository.deleteByToken(hashedToken);
    }

    private Instant getExpireTime() {
        return Instant.now().plusSeconds(refreshTokenTtl);
    }
}
