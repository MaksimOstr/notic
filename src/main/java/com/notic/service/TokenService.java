package com.notic.service;

import com.notic.dto.response.TokenResponse;
import com.notic.entity.RefreshToken;
import com.notic.entity.Role;
import com.notic.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtTokenService;

    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        RefreshToken validatedRefreshToken = refreshTokenService.validate(refreshToken);
        User user = validatedRefreshToken.getUser();
        String newRefreshToken = refreshTokenService.updateRefreshToken(validatedRefreshToken);

        return new TokenResponse(getAccessToken(user), newRefreshToken);
    }

    @Transactional
    public TokenResponse getTokenPair(User user) {
        String refreshToken = refreshTokenService.getRefreshToken(user);
        return new TokenResponse(getAccessToken(user), refreshToken);
    }

    private String getAccessToken(User user) {
        Set<String> roleNames = mapRoles(user.getRoles());
        return jwtTokenService.getJwsToken(roleNames, user.getId());
    }

    private Set<String> mapRoles(Set<Role> userRoles) {
        return userRoles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
