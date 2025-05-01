package com.notic.dto;

import com.notic.entity.User;

public record CreateProfileDto(
        String username,
        String avatar,
        User user
) {}
