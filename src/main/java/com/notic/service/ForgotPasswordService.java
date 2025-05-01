package com.notic.service;

import com.notic.enums.VerificationCodeScopeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {
    private final VerificationCodeService verificationCodeService;
    private final VerificationNotificationService verificationNotificationService;
    private final UserService userService;


    public void requestPasswordReset(String email) {
        verificationNotificationService.createAndSendCode(email, VerificationCodeScopeEnum.PASSWORD_RESET);
    }


    public void verifyCodeAndChangePassword(int code, String newPassword) {
        long userId = verificationCodeService.validate(code, VerificationCodeScopeEnum.PASSWORD_RESET);

        userService.updatePassword(userId, newPassword);
    }
}
