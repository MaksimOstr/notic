package com.notic.service;

import com.notic.enums.AuthProviderEnum;
import com.notic.enums.VerificationCodeScopeEnum;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.ForgotPasswordException;
import com.notic.projection.UserAuthProviderProjection;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {
    private final VerificationCodeService verificationCodeService;
    private final VerificationNotificationService verificationNotificationService;
    private final UserService userService;


    @Transactional
    public void requestPasswordReset(String email) {
        UserAuthProviderProjection user = userService.getUserAuthProviderByEmail(email)
                        .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));

        if(user.getAuthProvider() != AuthProviderEnum.LOCAL) {
            throw new ForgotPasswordException("User was not registered by Local provider");
        }

        verificationNotificationService.createAndSendCode(email, VerificationCodeScopeEnum.PASSWORD_RESET);
    }


    public void verifyCodeAndChangePassword(int code, String newPassword) {
        long userId = verificationCodeService.validate(code, VerificationCodeScopeEnum.PASSWORD_RESET);

        userService.updatePassword(userId, newPassword);
    }
}
