package com.notic.unit.service;

import com.notic.enums.AuthProviderEnum;
import com.notic.enums.VerificationCodeScopeEnum;
import com.notic.exception.ForgotPasswordException;
import com.notic.projection.UserAuthProviderProjection;
import com.notic.service.ForgotPasswordService;
import com.notic.service.UserService;
import com.notic.service.VerificationCodeService;
import com.notic.service.VerificationNotificationService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordServiceTest {
    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private VerificationNotificationService verificationNotificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    @Nested
    class RequestPasswordReset {
        String email = "<EMAIL>";
        UserAuthProviderProjection projection = mock(UserAuthProviderProjection.class);

        @Test
        void shouldCreateAndSendCode() {
            when(projection.getAuthProvider()).thenReturn(AuthProviderEnum.LOCAL);

            when(userService.getUserAuthProviderByEmail(anyString())).thenReturn(Optional.of(projection));
            doNothing().when(verificationNotificationService).createAndSendCode(anyString(), any());

            forgotPasswordService.requestPasswordReset(email);

            verify(userService).getUserAuthProviderByEmail(email);
            verify(verificationNotificationService).createAndSendCode(email, VerificationCodeScopeEnum.PASSWORD_RESET);
        }

        @Test
        void shouldThrowExceptionWhenUserIsNotLocal() {
            when(projection.getAuthProvider()).thenReturn(AuthProviderEnum.GOOGLE);
            when(userService.getUserAuthProviderByEmail(anyString())).thenReturn(Optional.of(projection));

            assertThrows(ForgotPasswordException.class, () -> forgotPasswordService.requestPasswordReset(email));

            verify(userService).getUserAuthProviderByEmail(email);
            verifyNoInteractions(verificationNotificationService);
        }
    }

    @Nested
    class VerifyCodeAndChangePassword {
        int code = 123456;
        String newPassword = "<PASSWORD>";

        @Test
        void shouldNotUpdateUserPassword() {
           when(verificationCodeService.validate(anyInt(), any())).thenThrow(new RuntimeException(""));

           assertThrows(RuntimeException.class, () -> forgotPasswordService.verifyCodeAndChangePassword(code, newPassword));

           verify(verificationCodeService).validate(code, VerificationCodeScopeEnum.PASSWORD_RESET);
           verifyNoInteractions(userService);
        }

        @Test
        void shouldUpdateUserPassword() {
            long userId = 123456L;
            when(verificationCodeService.validate(anyInt(), any())).thenReturn(userId);

            forgotPasswordService.verifyCodeAndChangePassword(code, newPassword);

            verify(verificationCodeService).validate(code, VerificationCodeScopeEnum.PASSWORD_RESET);
            verify(userService).updatePassword(userId, newPassword);
        }
    }
}
