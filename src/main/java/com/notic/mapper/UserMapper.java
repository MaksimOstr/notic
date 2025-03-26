package com.notic.mapper;


import com.notic.dto.UserDto;
import com.notic.entity.User;
import com.notic.projection.UserCredentialsProjection;
import com.notic.config.security.model.CustomUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    UserDto toDto(User user);

    @Mapping(source = "roles", target = "authorities")
    CustomUserDetails toCustomUserDetails(UserCredentialsProjection user);
}
