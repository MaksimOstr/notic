package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.dto.UserDto;
import com.notic.entity.User;
import com.notic.exception.UserAlreadyExistsException;
import com.notic.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Transactional(rollbackFor = UserAlreadyExistsException.class)
    public UserDto signUp(CreateUserDto body) {
       User createdUser = userService.createUser(body);

       return userMapper.toDto(createdUser);
   }

}
