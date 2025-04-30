package com.notic.unit.service;

import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.VerificationCodeException;
import com.notic.repository.VerificationCodeRepository;
import com.notic.service.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerificationCodeServiceTest {
    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @Nested
    class Create {
        User user = new User();

        @Test
        void shouldThrowDataIntegrityException() {
            when(verificationCodeRepository.existsByCode(anyInt())).thenReturn(false);
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenThrow(new DataIntegrityViolationException(""));

            assertThrows(EntityAlreadyExistsException.class, () -> verificationCodeService.create(user));
        }

        @Test
        void shouldCreateCode() {
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setUser(user);

            when(verificationCodeRepository.existsByCode(anyInt())).thenReturn(false);
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

            VerificationCode result = verificationCodeService.create(user);

            assertNotNull(result);
            assertEquals(user, result.getUser());
            assertInstanceOf(Integer.class, result.getCode());
        }

        @Test
        void shouldThrowExceptionWhenCodeAlreadyExists() {
            when(verificationCodeRepository.existsByCode(anyInt())).thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class, () -> verificationCodeService.create(user));

            verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
            verify(verificationCodeRepository, times(5)).existsByCode(anyInt());
        }
    }

    @Nested
    class Validate {
        int code = 123456;
        long codeId = 123424L;
        long userId = 2342341L;
        VerificationCode verificationCode = new VerificationCode();
        User user = new User();

        @BeforeEach
        void setUp() {
            user.setId(userId);
            verificationCode.setUser(user);
            verificationCode.setId(codeId);
            verificationCode.setCode(code);
            verificationCode.setExpiresAt(Instant.now().plusSeconds(3600));
        }



        @Test
        void successfulValidation() {
            when(verificationCodeRepository.findByCode(anyInt())).thenReturn(Optional.of(verificationCode));
            doNothing().when(verificationCodeRepository).deleteById(anyLong());

            long result = verificationCodeService.validate(code);

            verify(verificationCodeRepository).findByCode(code);
            verify(verificationCodeRepository).deleteById(codeId);

            assertEquals(userId, result);
            assertEquals(code, verificationCode.getCode());
        }

        @Test
        void shouldThrowExceptionWhenCodeDoesNotExist() {
            when(verificationCodeRepository.findByCode(anyInt())).thenReturn(Optional.empty());

            assertThrows(VerificationCodeException.class, () -> verificationCodeService.validate(code));

            verify(verificationCodeRepository).findByCode(code);
            verifyNoMoreInteractions(verificationCodeRepository);
        }

        @Test
        void shouldThrowExceptionWhenCodeIsExpired() {
            verificationCode.setExpiresAt(Instant.now().minusSeconds(3600));

            when(verificationCodeRepository.findByCode(anyInt())).thenReturn(Optional.of(verificationCode));

            assertThrows(VerificationCodeException.class, () -> verificationCodeService.validate(code));

            verify(verificationCodeRepository).findByCode(code);
            verifyNoMoreInteractions(verificationCodeRepository);
        }
    }
}
