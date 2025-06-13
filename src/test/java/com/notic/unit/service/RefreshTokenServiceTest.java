package com.notic.unit.service;

import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import com.notic.exception.TokenValidationException;
import com.notic.repository.RefreshTokenRepository;
import com.notic.service.RefreshTokenService;
import jakarta.persistence.EntityManager;
import org.hibernate.sql.ast.tree.expression.CaseSimpleExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshSecret", "1212");
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenTtl", 3600);
    }

    @Nested
    class GetRefreshToken {
        private final long userId = 1L;
        private final User user = new User();

        @BeforeEach
        void setUp() {
            user.setId(userId);
        }

        @Test
        void tokenAlreadyExists() {

            when(entityManager.getReference(User.class, userId)).thenReturn(user);
            when(refreshTokenRepository.findByUser_Id(anyLong())).thenReturn(Optional.empty());

            String result = refreshTokenService.create(userId);

            verify(refreshTokenRepository).findByUser_Id(userId);
            verify(refreshTokenRepository).save(refreshTokenArgumentCaptor.capture());

            assertNotNull(result);
            assertEquals(refreshTokenArgumentCaptor.getValue().getUser().getId(), userId);
            assertNotEquals(refreshTokenArgumentCaptor.getValue().getToken(), result);
        }

        @Test
        void tokenDoesNotExist() {
            when(refreshTokenRepository.findByUser_Id(anyLong())).thenReturn(Optional.of(new RefreshToken()));

            String result = refreshTokenService.create(userId);

            verify(refreshTokenRepository).findByUser_Id(userId);
            verifyNoMoreInteractions(refreshTokenRepository);

            assertNotNull(result);
        }
    }

    @Nested
    class Validate {
        private final User user = new User();
        private final String refreshToken = "refreshToken";
        private final String hashedRefreshToken = "hashedRefreshToken";
        private RefreshToken refreshTokenEntity;

        @BeforeEach
        void setUp() {
            refreshTokenEntity = new RefreshToken();
            refreshTokenEntity.setUser(user);
            refreshTokenEntity.setToken(hashedRefreshToken);
            refreshTokenEntity.setExpiresAt(Instant.now().plusSeconds(3600));
        }

        @Test
        void tokenDoesNotExist() {
            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            assertThrows(TokenValidationException.class, () -> refreshTokenService.validate(refreshToken));
            verify(refreshTokenRepository).findByToken(stringCaptor.capture());

            assertNotEquals(refreshToken, stringCaptor.getValue());
            verifyNoMoreInteractions(refreshTokenRepository);
        }

        @Test
        void tokenIsExpired() {
            long refreshTokenId = 1L;
            refreshTokenEntity.setId(refreshTokenId);
            refreshTokenEntity.setExpiresAt(Instant.now().minusSeconds(3600));

            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshTokenEntity));

            assertThrows(TokenValidationException.class, () -> refreshTokenService.validate(refreshToken));

            verify(refreshTokenRepository).findByToken(stringCaptor.capture());
            verify(refreshTokenRepository).deleteById(refreshTokenId);

            assertNotEquals(refreshToken, stringCaptor.getValue());
        }


        @Test
        void successfulValidation() {
            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshTokenEntity));

            RefreshToken result = refreshTokenService.validate(refreshToken);

            verify(refreshTokenRepository, never()).deleteById(anyLong());
            verify(refreshTokenRepository).findByToken(stringCaptor.capture());

            assertNotNull(result);
            assertEquals(hashedRefreshToken, refreshTokenEntity.getToken());
            assertNotEquals(result.getToken(), stringCaptor.getValue());
        }
    }
}
