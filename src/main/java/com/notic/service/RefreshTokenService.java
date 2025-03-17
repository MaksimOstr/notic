package com.notic.service;

import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(rollbackFor = EntityAlreadyExistsException.class)
    public RefreshToken getRefreshToken(User user) {
        refreshTokenRepository.deleteAllByUser(user);
        String token = generateToken();

        if(refreshTokenRepository.existsByToken(token)) {
            throw new EntityAlreadyExistsException("Refresh token already exists");
        }

        RefreshToken refreshToken = new RefreshToken(
                token,
                user,
                generateExpireTime()
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public Cookie getRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        cookie.setHttpOnly(true);
        cookie.setMaxAge(60*60);
        cookie.setPath("/");

        return cookie;
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private Instant generateExpireTime() {
        return Instant.now().plusSeconds(3600);
    }

}
