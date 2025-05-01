package com.notic.unit.service;

import com.notic.enums.VerificationCodeScopeEnum;
import com.notic.exception.VerificationCodeException;
import com.notic.service.EmailVerificationService;
import com.notic.service.UserService;
import com.notic.service.VerificationCodeService;
import com.notic.service.VerificationNotificationService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {
    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private VerificationNotificationService verificationNotificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    void requestEmailVerification() {
        String email = "<EMAIL>";

        doNothing().when(verificationNotificationService).createAndSendCode(anyString(), any());

        emailVerificationService.requestEmailVerification(email);

        verify(verificationNotificationService).createAndSendCode(email, VerificationCodeScopeEnum.EMAIL_VERIFICATION);
    }

    @Nested
    class VerifyCodeAndEnableUser {
        int code = 123456;

        @Test
        void shouldThrowInvalidCodeAndDoNotEnableUser() {
            when(verificationCodeService.validate(anyInt(), any())).thenThrow(new VerificationCodeException(""));

            assertThrows(VerificationCodeException.class, () -> emailVerificationService.verifyCodeAndEnableUser(code));

            verify(verificationCodeService).validate(code, VerificationCodeScopeEnum.EMAIL_VERIFICATION);
            verifyNoInteractions(userService);
        }

        @Test
        void shouldEnableUser() {
            long userId = 123456L;
            when(verificationCodeService.validate(anyInt(), any())).thenReturn(userId);

            emailVerificationService.verifyCodeAndEnableUser(code);

            verify(verificationCodeService).validate(code, VerificationCodeScopeEnum.EMAIL_VERIFICATION);
            verify(userService).markUserAsVerified(userId);
        }
    }
}
