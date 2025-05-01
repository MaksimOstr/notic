package com.notic.unit.service;

import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.enums.VerificationCodeScopeEnum;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.service.MailService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerificationNotificationServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private VerificationNotificationService verificationService;

    @Nested
    class CreateAndSendVerificationCode {
        String email = "<EMAIL>";
        VerificationCodeScopeEnum scope = VerificationCodeScopeEnum.EMAIL_VERIFICATION;

        @Test
        void shouldThrowExceptionUserDoesNotExist() {
            when(userService.getUserByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(EntityDoesNotExistsException.class, () -> verificationService.createAndSendCode(email, scope));

            verify(userService).getUserByEmail(email);
            verifyNoInteractions(mailService, verificationCodeService);
        }

        @Test
        void shouldCreateAndSendCode() {
            User user = new User();
            user.setEmail(email);
            String subject = "Email verification code";
            int code = 123456;
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setCode(code);
            verificationCode.setScope(scope);

            when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(user));
            when(verificationCodeService.create(any(User.class), any(VerificationCodeScopeEnum.class))).thenReturn(verificationCode);

            verificationService.createAndSendCode(email, scope);

            verify(userService).getUserByEmail(email);
            verify(verificationCodeService).create(user, scope);
            verify(mailService).send(user.getEmail(), subject, String.valueOf(verificationCode.getCode()));
        }
    }
}
