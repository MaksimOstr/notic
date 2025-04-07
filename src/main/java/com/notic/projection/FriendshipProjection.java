package com.notic.projection;

import java.time.Instant;

public record FriendshipProjection(
        long friendId,
        String friendName,
        String friendAvatar,
        Instant friendshipDate
) {
}
