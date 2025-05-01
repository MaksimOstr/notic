package com.notic.dto.request;

import com.notic.enums.NoteVisibilityEnum;
import com.notic.validators.ValueOfEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNoteDto(

        @NotBlank
        @Size(min=2, max = 255)
        String title,

        @Size(max = 2000, message = "Content too long")
        String content,

        @NotBlank
        @ValueOfEnum(enumClass = NoteVisibilityEnum.class)
        String visibility
) {
}
