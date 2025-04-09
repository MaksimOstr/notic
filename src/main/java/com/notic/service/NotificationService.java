package com.notic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String USER_NOTIFICATION_ENDPOINT = "/queue/notifications";

    private record FriendshipRequestNotification(
            String username,
            String avatar,
            String message
    ) {}


    public void friendshipRequested(String receiverId, String username, String avatar) {
        String message = username + " has sent you a friendship request";
        FriendshipRequestNotification notification = new FriendshipRequestNotification(
                username,
                avatar,
                message
        );

        messagingTemplate.convertAndSendToUser(
                receiverId,
                USER_NOTIFICATION_ENDPOINT,
                notification
        );
    }


    public void friendshipRequestAccepted(String senderId, String username, String avatar) {
        String message = username + " accepted your friendship request";
        FriendshipRequestNotification notification = new FriendshipRequestNotification(
                username,
                avatar,
                message
        );

        messagingTemplate.convertAndSendToUser(
                senderId,
                USER_NOTIFICATION_ENDPOINT,
                notification
        );
    }
}
