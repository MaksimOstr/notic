package com.notic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
        @Schema(
                description = "User email",
                example = "user@example.com"
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "Username",
                example = "Bob",
                minLength = 3,
                maxLength = 20
        )
        @NotBlank
        @Size(min=3, max=20)
        String username,

        @Schema(
                description = "Password",
                example = "12345678",
                minLength = 8,
                maxLength = 100
        )
        @NotBlank
        @Size(min=8, max=100)
        String password
) {}
