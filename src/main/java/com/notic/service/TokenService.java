package com.notic.service;

import com.notic.dto.response.TokenResponse;
import com.notic.entity.RefreshToken;
import com.notic.entity.Role;
import com.notic.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static com.notic.utils.UserUtils.mapUserRoles;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtTokenService;
    private final EntityManager entityManager;

    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        RefreshToken validatedRefreshToken = refreshTokenService.validate(refreshToken);
        User user = validatedRefreshToken.getUser();
        String newRefreshToken = refreshTokenService.updateRefreshToken(validatedRefreshToken);
        return new TokenResponse(getAccessToken(user.getId(), mapUserRoles(user)), newRefreshToken);
    }

    @Transactional
    public TokenResponse getTokenPair(long userId, Set<String> userRoles) {
        User user = entityManager.getReference(User.class, userId);
        String refreshToken = refreshTokenService.create(user);
        return new TokenResponse(getAccessToken(userId, userRoles), refreshToken);
    }

    private String getAccessToken(long userId, Set<String> roleNames) {
        return jwtTokenService.getJwsToken(roleNames, userId);
    }

}
