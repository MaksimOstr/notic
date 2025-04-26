package com.notic.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificationCodeRequestDto(
        @Size(min = 8, max = 8)
        @NotNull
        String code
) {}
