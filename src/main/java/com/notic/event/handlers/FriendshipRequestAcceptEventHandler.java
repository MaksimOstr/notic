package com.notic.event.handlers;


import com.notic.event.FriendshipRequestAcceptEvent;
import com.notic.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendshipRequestAcceptEventHandler {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handle(FriendshipRequestAcceptEvent event) {
        notificationService.friendshipRequestAccepted(
                event.senderId(),
                event.username(),
                event.avatarUrl()
        );
    }
}
