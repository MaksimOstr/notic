package com.notic.mapper;


import com.notic.dto.CreateProfileDto;
import com.notic.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "username", source = "username")
    CreateProfileDto toCreateProfileDto(User user, String username);
}
