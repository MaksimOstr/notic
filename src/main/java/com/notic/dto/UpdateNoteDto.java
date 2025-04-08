package com.notic.dto;


import com.notic.enums.NoteVisibilityEnum;
import com.notic.validators.ValueOfEnum;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RequiredArgsConstructor
public class UpdateNoteDto {
    @Size(max = 255, message = "Title too long")
    private final String title;

    @Size(max = 2000, message = "Content too long")
    private final String content;

    @ValueOfEnum(enumClass = NoteVisibilityEnum.class)
    private final String visibility;


    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }
    public Optional<String> getContent() {
        return Optional.ofNullable(content);
    }
    public Optional<String> getVisibility() {
        return Optional.ofNullable(visibility);
    }
}
