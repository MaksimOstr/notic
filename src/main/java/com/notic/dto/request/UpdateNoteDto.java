package com.notic.dto.request;


import com.notic.enums.NoteVisibilityEnum;
import com.notic.validators.ValueOfEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class UpdateNoteDto {
    @NotBlank
    @Size(min=2, max = 255, message = "Title too long")
    private final String title;

    @Size(max = 2000, message = "Content too long")
    private final String content;

    @ValueOfEnum(enumClass = NoteVisibilityEnum.class)
    private final String visibility;
}
