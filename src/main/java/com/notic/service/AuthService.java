package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.dto.SignInDto;
import com.notic.dto.UserDto;
import com.notic.entity.User;
import com.notic.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtTokenService;

    public UserDto signUp(CreateUserDto body) {
       User createdUser = userService.createUser(body);

       return userMapper.toDto(createdUser);
   }


   public String signIn(SignInDto body) {
        Authentication authReq = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        Authentication authResult = authenticationManager.authenticate(authReq);
        return jwtTokenService.getJwsToken(authResult.getAuthorities(), authResult.getName());
   }

}
