package com.notic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationEmailRequestDto(
        @Size(min = 8, max = 8)
        @NotBlank
        String code
) {}
