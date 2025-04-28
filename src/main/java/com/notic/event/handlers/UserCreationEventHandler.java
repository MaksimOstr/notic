package com.notic.event.handlers;

import com.notic.event.UserCreationEvent;
import com.notic.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreationEventHandler {

    private final VerificationService verificationService;

    @EventListener
    public void handle(UserCreationEvent event) {
        verificationService.createAndSendVerificationCode(event.email());
    }
}
