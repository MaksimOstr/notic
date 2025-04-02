package com.notic.dto;

import jakarta.validation.constraints.NotNull;

public record CreateFriendshipDto(

        @NotNull
        Long receiverId
) {}
