package com.notic.dto.request;

import com.notic.enums.NoteVisibilityEnum;
import java.time.LocalDate;
import java.util.List;

public record NotesByFiltersRequest (
        String title,
        String content,
        List<String> visibility,
        LocalDate createdAt
) {
    public List<NoteVisibilityEnum> getVisibility() {
        if(visibility == null) return List.of();
        return visibility.stream().map(NoteVisibilityEnum::fromString).toList();
    }
}
