package com.notic.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
