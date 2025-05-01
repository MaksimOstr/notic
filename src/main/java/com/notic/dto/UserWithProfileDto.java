package com.notic.dto;

import com.notic.entity.Profile;
import com.notic.entity.User;

public record UserWithProfileDto(
        User user,
        Profile profile
) {
}
