package com.notic.unit.service;

import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.enums.VerificationCodeScopeEnum;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.VerificationCodeException;
import com.notic.repository.VerificationCodeRepository;
import com.notic.service.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<VerificationCode> codeArgumentCaptor;

    @Nested
    class Create {
        User user = new User();
        VerificationCodeScopeEnum scope = VerificationCodeScopeEnum.EMAIL_VERIFICATION;

        @Test
        void shouldThrowDataIntegrityException() {
            when(verificationCodeRepository.existsByCodeAndScope(anyInt(), any())).thenReturn(false);
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenThrow(new DataIntegrityViolationException(""));

            assertThrows(EntityAlreadyExistsException.class, () -> verificationCodeService.create(user, scope));

            verify(verificationCodeRepository).existsByCodeAndScope(anyInt(), any(VerificationCodeScopeEnum.class));
        }

        @Test
        void shouldCreateCode() {
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setUser(user);
            verificationCode.setScope(scope);

            when(verificationCodeRepository.existsByCodeAndScope(anyInt(), any())).thenReturn(false);
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

            VerificationCode result = verificationCodeService.create(user, scope);

            verify(verificationCodeRepository).existsByCodeAndScope(anyInt(), any(VerificationCodeScopeEnum.class));
            verify(verificationCodeRepository).save(codeArgumentCaptor.capture());

            VerificationCode savedCode = codeArgumentCaptor.getValue();

            assertNotNull(result);
            assertEquals(user, result.getUser());
            assertInstanceOf(Integer.class, result.getCode());
            assertEquals(scope, savedCode.getScope());
            assertEquals(user, savedCode.getUser());
        }

        @Test
        void shouldThrowExceptionWhenCodeAlreadyExists() {
            when(verificationCodeRepository.existsByCodeAndScope(anyInt(), any())).thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class, () -> verificationCodeService.create(user, scope));

            verify(verificationCodeRepository, times(5)).existsByCodeAndScope(anyInt(), any(VerificationCodeScopeEnum.class));
            verifyNoMoreInteractions(verificationCodeRepository);
        }
    }

    @Nested
    class Validate {
        int code = 123456;
        long codeId = 123424L;
        long userId = 234212341L;
        VerificationCode verificationCode = new VerificationCode();
        VerificationCodeScopeEnum scope = VerificationCodeScopeEnum.EMAIL_VERIFICATION;
        User user = new User();

        @BeforeEach
        void setUp() {
            user.setId(userId);
            verificationCode.setUser(user);
            verificationCode.setId(codeId);
            verificationCode.setScope(scope);
            verificationCode.setCode(code);
            verificationCode.setExpiresAt(Instant.now().plusSeconds(3600));
        }


        @Test
        void successfulValidation() {
            when(verificationCodeRepository.findByCodeAndScope(anyInt(), any())).thenReturn(Optional.of(verificationCode));
            doNothing().when(verificationCodeRepository).deleteById(anyLong());

            long result = verificationCodeService.validate(code, scope);

            verify(verificationCodeRepository).findByCodeAndScope(code, scope);
            verify(verificationCodeRepository).deleteById(codeId);

            assertEquals(userId, result);
            assertEquals(code, verificationCode.getCode());
        }

        @Test
        void shouldThrowExceptionWhenCodeDoesNotExist() {
            when(verificationCodeRepository.findByCodeAndScope(anyInt(), any())).thenReturn(Optional.empty());

            assertThrows(VerificationCodeException.class, () -> verificationCodeService.validate(code, scope));

            verify(verificationCodeRepository).findByCodeAndScope(code, scope);
            verifyNoMoreInteractions(verificationCodeRepository);
        }

        @Test
        void shouldThrowExceptionWhenCodeIsExpired() {
            verificationCode.setExpiresAt(Instant.now().minusSeconds(3600));

            when(verificationCodeRepository.findByCodeAndScope(anyInt(), any())).thenReturn(Optional.of(verificationCode));
            doNothing().when(verificationCodeRepository).deleteById(anyLong());

            assertThrows(VerificationCodeException.class, () -> verificationCodeService.validate(code, scope));

            verify(verificationCodeRepository).findByCodeAndScope(code, scope);
            verify(verificationCodeRepository).deleteById(codeId);
        }
    }
}
