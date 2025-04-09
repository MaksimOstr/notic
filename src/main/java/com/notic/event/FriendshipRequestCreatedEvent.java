package com.notic.event;

public record FriendshipRequestCreatedEvent(
        String receiverId,
        String username,
        String avatarUrl
) {}
