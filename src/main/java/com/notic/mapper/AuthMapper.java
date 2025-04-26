package com.notic.mapper;

import com.notic.dto.CreateLocalUserDto;
import com.notic.dto.request.SignUpRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    CreateLocalUserDto signUptoCreateUserDto(SignUpRequestDto signUpDto);
}
