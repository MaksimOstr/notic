package com.notic.dto;

public record CreateLocalUserDto(
        String email,
        String password,
        String username
) {}
