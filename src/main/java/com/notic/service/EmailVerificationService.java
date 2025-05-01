package com.notic.service;

import com.notic.enums.VerificationCodeScopeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final VerificationNotificationService verificationNotificationService;


    public void requestEmailVerification(String email) {
        verificationNotificationService.createAndSendCode(email, VerificationCodeScopeEnum.EMAIL_VERIFICATION);
    }

    @Transactional
    public void verifyCodeAndEnableUser(int code) {
        long userId = verificationCodeService.validate(code, VerificationCodeScopeEnum.EMAIL_VERIFICATION);
        System.out.println("erfcsdfsdfsdf");
        userService.markUserAsVerified(userId);
    }
}
