package com.notic.unit.service;

import com.notic.dto.response.TokenResponse;
import com.notic.entity.RefreshToken;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.TokenValidationException;
import com.notic.service.JwtService;
import com.notic.service.RefreshTokenService;
import com.notic.service.TokenService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TokenService tokenService;

    @Nested
    class RefreshTokens {
        private final String suppliedRefresh = "suppliedRefresh";

        @Test
        void shouldRefreshTokens() {
            String newRefreshToken = "newRefreshToken";
            String refreshTokenToSave = "hashedRefreshToken";
            String accessToken = "accessToken";
            long userId = 1123L;
            Role role = new Role("USER");
            User user = new User();
            user.setRoles(Set.of(role));
            user.setId(userId);
            RefreshToken validatedRefreshToken = new RefreshToken(
                    refreshTokenToSave,
                    user,
                    Instant.now()
            );

            when(refreshTokenService.validate(anyString())).thenReturn(validatedRefreshToken);
            when(refreshTokenService.updateRefreshToken(any(RefreshToken.class))).thenReturn(newRefreshToken);
            when(jwtService.getJwsToken(anySet(), anyLong())).thenReturn(accessToken);

            TokenResponse result = tokenService.refreshTokens(suppliedRefresh);

            verify(refreshTokenService).validate(suppliedRefresh);
            verify(refreshTokenService).updateRefreshToken(validatedRefreshToken);
            verify(jwtService).getJwsToken(Set.of(role.getName()), userId);

            assertEquals(accessToken, result.accessToken());
            assertEquals(newRefreshToken, result.refreshToken());
            assertNotEquals(suppliedRefresh, result.refreshToken());
            assertNotEquals(refreshTokenToSave, result.refreshToken());
        }

        @Test
        void shouldNotRefreshTokensRefreshIsInvalid() {
            when(refreshTokenService.validate(anyString())).thenThrow(new TokenValidationException(""));

            assertThrows(TokenValidationException.class, () -> tokenService.refreshTokens(suppliedRefresh));

            verify(refreshTokenService).validate(suppliedRefresh);
            verifyNoInteractions(jwtService);
            verifyNoMoreInteractions(refreshTokenService);
        }
    }

    @Test
    void getTokenPait() {
        long userId = 1123L;
        Set<String> roles = Set.of("USER", "ADMIN");
        String createdRefreshToken = "createdRefreshToken";
        String accessToken = "accessToken";
        User user = new User();
        user.setId(userId);

        when(entityManager.getReference(User.class, userId)).thenReturn(user);
        when(refreshTokenService.create(any(User.class))).thenReturn(createdRefreshToken);
        when(jwtService.getJwsToken(anySet(), anyLong())).thenReturn(accessToken);

        TokenResponse result = tokenService.getTokenPair(userId, roles);

        verify(entityManager).getReference(User.class, userId);
        verify(refreshTokenService).create(user);
        verify(jwtService).getJwsToken(roles, userId);

        assertNotNull(result);
        assertEquals(accessToken, result.accessToken());
        assertEquals(createdRefreshToken, result.refreshToken());
    }
}
