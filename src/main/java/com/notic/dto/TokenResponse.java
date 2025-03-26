package com.notic.dto;

import jakarta.servlet.http.Cookie;

public record TokenResponse(
        String accessToken,
        Cookie refreshTokenCookie
) {}
