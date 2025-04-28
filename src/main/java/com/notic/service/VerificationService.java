package com.notic.service;

import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.exception.EntityDoesNotExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationCodeService verificationCodeService;
    private final MailService mailService;
    private final UserService userService;

    @Transactional
    public void createAndSendVerificationCode(String email) {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));

        VerificationCode verificationCode = verificationCodeService.create(user);
        String subject = "Verification email code";
        mailService.send(
                user.getEmail(),
                subject,
                String.valueOf(verificationCode.getCode())
        );
    }

    @Transactional
    public void verifyCode(long code) {
        long userId = verificationCodeService.validate(code);

        userService.markUserAsVerified(userId);
    }
}
