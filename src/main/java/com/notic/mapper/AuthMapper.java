package com.notic.mapper;

import com.notic.dto.CreateUserDto;
import com.notic.dto.SignUpRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    CreateUserDto toCreateUserDto(SignUpRequestDto signUpDto);
}
