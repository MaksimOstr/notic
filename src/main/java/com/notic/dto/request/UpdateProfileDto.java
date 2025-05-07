package com.notic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileDto(
        @NotBlank
        @Size(min=3, max=20)
        String username
) { }
