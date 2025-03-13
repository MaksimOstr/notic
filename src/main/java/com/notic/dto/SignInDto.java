package com.notic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record SignInDto(
        @Email
        String email,

        @NotBlank
        String password
) {}
