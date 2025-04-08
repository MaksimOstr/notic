package com.notic.event;

public record FriendshipRequestAcceptEvent(
        String senderId,
        String username,
        String avatar
) {}
