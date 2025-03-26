package com.notic.dto;

import com.notic.entity.RefreshToken;

public record RefreshTokenValidationResultDto(
        RefreshToken refreshToken,
        String rawRefreshToken
) {
}
