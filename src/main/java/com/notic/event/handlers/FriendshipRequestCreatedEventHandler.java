package com.notic.event.handlers;

import com.notic.event.FriendshipRequestCreatedEvent;
import com.notic.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class FriendshipRequestCreatedEventHandler {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void Handle(FriendshipRequestCreatedEvent event) {
        notificationService.friendshipRequested(
                event.receiverId(),
                event.username(),
                event.avatarUrl()
        );
    }
}
