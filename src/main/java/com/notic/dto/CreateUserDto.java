package com.notic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDto(
        @Email
        String email,

        @NotBlank
        @Size(min=3, max=20)
        String username,

        @NotBlank
        @Size(min=8, max=100)
        String password
) {}
