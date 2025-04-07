package com.notic.projection;

import java.time.Instant;

public record FriendshipRequestProjection(
        long requestId,
        String senderUsername,
        String senderAvatar,
        Instant requestTime
) {
}
