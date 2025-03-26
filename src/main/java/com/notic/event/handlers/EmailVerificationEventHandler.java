package com.notic.event.handlers;

import com.notic.event.EmailVerificationEvent;
import com.notic.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationEventHandler {

    private final EmailVerificationService emailVerificationService;

    @Async
    @EventListener
    public void handle(EmailVerificationEvent event) {
        log.info("Email verification event received");
        emailVerificationService.sendSimpleEmail(
                event.email(),
                "Email verification",
                String.valueOf(event.code())
        );
    }

}
