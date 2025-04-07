package com.notic.dto;

import jakarta.validation.constraints.NotNull;

public record SendFriendshipRequestDto(
        @NotNull
        Long receiverId
) {}
