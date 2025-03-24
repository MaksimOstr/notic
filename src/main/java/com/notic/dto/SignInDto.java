package com.notic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record SignInDto(
        @Schema(
                description = "User email",
                example = "user@example.com"
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "Password",
                example = "12345678"
        )
        @NotBlank
        String password
) {}
