package com.notic.unit.service;

import com.notic.dto.RefreshTokenValidationResultDto;
import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import com.notic.exception.TokenValidationException;
import com.notic.repository.RefreshTokenRepository;
import com.notic.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenArgumentCaptor;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshSecret", "1212");
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenTtl", 3600);
    }

    @Nested
    class GetRefreshToken {
        private final User user = new User();
        @Test
        void tokenAlreadyExists() {

            when(refreshTokenRepository.findByUser(any(User.class))).thenReturn(Optional.empty());

            String result = refreshTokenService.getRefreshToken(user);

            verify(refreshTokenRepository, times(1)).findByUser(user);
            verify(refreshTokenRepository, times(1)).save(refreshTokenArgumentCaptor.capture());

            assertNotNull(result);
            assertEquals(refreshTokenArgumentCaptor.getValue().getUser(), user);
            assertNotEquals(refreshTokenArgumentCaptor.getValue().getToken(), result);
        }

        @Test
        void tokenDoesNotExist() {

            when(refreshTokenRepository.findByUser(any(User.class))).thenReturn(Optional.of(new RefreshToken()));

            String result = refreshTokenService.getRefreshToken(user);

            verify(refreshTokenRepository, times(1)).findByUser(user);
            verifyNoMoreInteractions(refreshTokenRepository);

            assertNotNull(result);
        }
    }

    @Test
    void getRefreshTokenCookie() {
        String refreshToken = "refreshToken";

        Cookie result = refreshTokenService.getRefreshTokenCookie(refreshToken);

        assertNotNull(result);
        assertEquals(refreshToken, result.getValue());
        assertEquals(3600, result.getMaxAge());
        assertTrue(result.getPath().startsWith("/"));
        assertTrue(result.isHttpOnly());
    }

    @Nested
    class ValidateAndRotateToken {
        private final User user = new User();
        private final String refreshToken = "refreshToken";
        private final String hashedRefreshToken = "hashedRefreshToken";
        @Test
        void tokenDoesNotExist() {
            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            Exception exception = assertThrows(TokenValidationException.class, () -> refreshTokenService.validateAndRotateToken(refreshToken));
            verify(refreshTokenRepository, times(1)).findByToken(anyString());
            verifyNoMoreInteractions(refreshTokenRepository);
            assertEquals("Refresh token not found", exception.getMessage());
        }

        @Test
        void tokenIsExpired() {
            RefreshToken refreshTokenEntity = new RefreshToken(hashedRefreshToken, user, Instant.now().minusSeconds(3700));

            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshTokenEntity));

            Exception exception = assertThrows(TokenValidationException.class, () -> refreshTokenService.validateAndRotateToken(refreshToken));

            verify(refreshTokenRepository, times(1)).findByToken(anyString());
            verify(refreshTokenRepository, times(1)).deleteById(anyLong());
            assertEquals("Session is expired", exception.getMessage());
        }


        @Test
        void succesfulValidateAndRotateToken() {
            RefreshToken refreshTokenEntity = new RefreshToken(hashedRefreshToken, user, Instant.now().plusSeconds(3600));
            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshTokenEntity));

            RefreshTokenValidationResultDto result = refreshTokenService.validateAndRotateToken(refreshToken);

            verify(refreshTokenRepository, times(1)).findByToken(anyString());
            verifyNoMoreInteractions(refreshTokenRepository);

            assertNotNull(result);
            assertNotEquals(refreshToken, result.refreshToken().getToken());
            assertNotEquals(hashedRefreshToken, result.refreshToken().getToken());
            assertNotEquals(refreshToken, result.rawRefreshToken());
            assertNotEquals(result.rawRefreshToken(), result.refreshToken().getToken());
        }
    }
}
