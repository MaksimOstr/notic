package com.notic.mapper;


import com.notic.dto.UserDto;
import com.notic.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    UserDto toDto(User user);
}
