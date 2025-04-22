package com.notic.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
