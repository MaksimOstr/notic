package com.notic.unit.service;

import com.notic.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;


    @Test
    public void sendFriendshipRequestNotification() throws Exception {
        String receiverId = UUID.randomUUID().toString();
        String username = UUID.randomUUID().toString();
        String avatar = UUID.randomUUID().toString();
        String expectedEndpoint = "/queue/notifications";
        String message = username + " has sent you a friendship request";

        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Object.class));

        notificationService.friendshipRequested(receiverId, username, avatar);

        verify(messagingTemplate).convertAndSendToUser(
                eq(receiverId),
                eq(expectedEndpoint),
                argThat(notification ->
                                notification.toString().contains(username) &&
                                notification.toString().contains(avatar) &&
                                notification.toString().contains(message))
        );
    }


    @Test
    public void friendshipRequestAccepted() throws Exception {
        String senderId = UUID.randomUUID().toString();
        String username = UUID.randomUUID().toString();
        String avatar = UUID.randomUUID().toString();
        String expectedEndpoint = "/queue/notifications";
        String message = username + " accepted your friendship request";

        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Object.class));

        notificationService.friendshipRequestAccepted(senderId, username, avatar);

        verify(messagingTemplate).convertAndSendToUser(
                eq(senderId),
                eq(expectedEndpoint),
                argThat(notification ->
                        notification.toString().contains(username) &&
                                notification.toString().contains(avatar) &&
                                notification.toString().contains(message))
        );
    }
}
