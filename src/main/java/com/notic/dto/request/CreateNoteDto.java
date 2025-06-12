package com.notic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNoteDto(

        @NotBlank
        @Size(min=2, max = 255)
        String title,

        @Size(max = 2000, message = "Content too long")
        String content,

        @NotBlank
        String visibility
) {
}
