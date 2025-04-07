package com.notic.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthUserDto {
    private final long userId;

    public long getId() {
        return userId;
    }
}
