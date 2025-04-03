package com.notic.projection;

import java.time.Instant;

public record FriendshipDto(
        long friendId,
        String friendName,
        String friendAvatar,
        Instant friendshipDate
) {
}
