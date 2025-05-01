package com.notic.unit.service;

import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.service.MailService;
import com.notic.service.UserService;
import com.notic.service.VerificationCodeService;
import com.notic.service.VerificationService;
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
public class VerificationServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private VerificationService verificationService;

    @Nested
    class CreateAndSendVerificationCode {
        String email = "<EMAIL>";

        @Test
        void shouldThrowExceptionUserDoesNotExist() {
            when(userService.getUserByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(EntityDoesNotExistsException.class, () -> verificationService.createAndSendVerificationCode(email));

            verify(userService).getUserByEmail(email);
            verifyNoInteractions(mailService, verificationCodeService);
        }

        @Test
        void shouldCreateAndSendCode() {
            User user = new User();
            user.setEmail(email);
            String subject = "Verification email code";
            int code = 123456;
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setCode(code);

            when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(user));
            when(verificationCodeService.create(any(User.class))).thenReturn(verificationCode);

            verificationService.createAndSendVerificationCode(email);

            verify(userService).getUserByEmail(email);
            verify(verificationCodeService).create(user);
            verify(mailService).send(user.getEmail(), subject, String.valueOf(verificationCode.getCode()));
        }
    }

    @Test
    void verifyCode() {
        int code = 12345;
        long userId = 1L;

        when(verificationCodeService.validate(anyInt())).thenReturn(userId);

        verificationService.verifyEmail(code);

        verify(verificationCodeService).validate(code);
        verify(userService).markUserAsVerified(userId);
    }
}
