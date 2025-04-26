package com.notic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendVerificationCodeRequestDto(
        @Email
        @NotBlank
        String email
) {}
