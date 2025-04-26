package com.notic.mapper;

import com.notic.dto.CreateProfileDto;
import com.notic.dto.CreateUserDto;
import com.notic.dto.request.SignUpRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    CreateUserDto signUptoCreateUserDto(SignUpRequestDto signUpDto);
    CreateProfileDto signUptoProfileDto(SignUpRequestDto signUpDto);
}
