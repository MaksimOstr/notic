package com.notic.dto;

import java.time.Instant;

public record FriendshipInfoDto(
        UserFriendDto user,
        Instant friendshipDate
) {
}
