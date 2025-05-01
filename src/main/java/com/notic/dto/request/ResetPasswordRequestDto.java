package com.notic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank
        @Size(min = 8, max = 8)
        String code,

        @NotBlank
        @Size(min=8, max=100)
        String newPassword
) {}
