package com.notic.dto.request;


import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UpdateNoteDto {
    @Size(min=2, max = 255, message = "Title too long")
    private final String title;

    @Size(max = 2000, message = "Content too long")
    private final String content;

    private final String visibility;
}
