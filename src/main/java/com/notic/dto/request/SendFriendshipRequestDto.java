package com.notic.dto.request;

import jakarta.validation.constraints.NotNull;

public record SendFriendshipRequestDto(
        @NotNull
        Long receiverId
) {}
