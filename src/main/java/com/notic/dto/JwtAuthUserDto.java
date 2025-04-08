package com.notic.dto;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class JwtAuthUserDto implements Principal {
    private final long userId;

    public long getId() {
        return userId;
    }

    @Override
    public String getName() {
        return Long.toString(userId);
    }
}
