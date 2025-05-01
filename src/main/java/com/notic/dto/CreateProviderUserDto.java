package com.notic.dto;

import com.notic.enums.AuthProviderEnum;

public record CreateProviderUserDto(
        AuthProviderEnum authProvider,
        String email,
        String username,
        String avatar
) {}
