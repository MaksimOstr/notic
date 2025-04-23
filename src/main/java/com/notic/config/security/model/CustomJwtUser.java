package com.notic.config.security.model;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomJwtUser {
    private final long userId;

    public long getId() {
        return userId;
    }
}
