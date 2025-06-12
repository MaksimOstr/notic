package com.notic.mapper;

import com.notic.dto.request.CreateNoteDto;
import com.notic.entity.Note;
import com.notic.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NoteMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", source = "user")
    @Mapping(target = "visibility", expression = "java(com.notic.enums.NoteVisibilityEnum.fromString(dto.visibility()))")
    Note toNote(CreateNoteDto dto, User user);
}
