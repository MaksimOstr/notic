package com.notic.event.handlers;

import com.notic.event.UserCreationEvent;
import com.notic.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreationEventHandler {

    private final EmailVerificationService emailVerificationService;

    @EventListener
    public void handle(UserCreationEvent event) {
        emailVerificationService.requestEmailVerification(event.email());
    }
}
