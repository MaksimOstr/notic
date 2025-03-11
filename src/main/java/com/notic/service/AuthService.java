package com.notic.service;

import com.notic.dto.SignUpDto;
import com.notic.dto.UserDto;
import com.notic.entity.User;
import com.notic.exception.UserAlreadyExistsException;
import com.notic.mapper.UserMapper;
import com.notic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Transactional(rollbackFor = UserAlreadyExistsException.class)
    public UserDto signUp(SignUpDto body) {
       if(userRepository.existsByEmail(body.email())) {
           throw new UserAlreadyExistsException("User with email " + body.email() + " already exists");
       }

       User user = new User(
               body.username(),
               body.email(),
               passwordEncoder.encode(body.password())
       );

       return userMapper.toDto(userRepository.save(user));
   }

}
