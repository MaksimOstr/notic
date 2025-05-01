package com.notic.service;

import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.enums.VerificationCodeScopeEnum;
import com.notic.exception.EntityDoesNotExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class VerificationNotificationService {

    private final VerificationCodeService verificationCodeService;
    private final MailService mailService;
    private final UserService userService;

    @Transactional
    public void createAndSendCode(String email, VerificationCodeScopeEnum scope) {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));

        VerificationCode verificationCode = verificationCodeService.create(user, scope);

        String subject = switch (scope) {
            case EMAIL_VERIFICATION -> "Email verification code";
            case PASSWORD_RESET -> "Password reset code";
        };

        mailService.send(
                user.getEmail(),
                subject,
                String.valueOf(verificationCode.getCode())
        );
    }
}
