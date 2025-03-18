package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.dto.SignInDto;
import com.notic.dto.TokenResponse;
import com.notic.dto.UserDto;
import com.notic.entity.RefreshToken;
import com.notic.entity.User;
import com.notic.mapper.UserMapper;
import com.notic.security.model.CustomUserDetails;
import jakarta.servlet.http.Cookie;
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
    private final RefreshTokenService refreshTokenService;

    public UserDto signUp(CreateUserDto body) {
       User createdUser = userService.createUser(body);

       return userMapper.toDto(createdUser);
   }


   public TokenResponse signIn(SignInDto body) {
        Authentication authReq = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        Authentication authResult = authenticationManager.authenticate(authReq);

        User user = userService.getUserByEmail(body.email());

        String refreshToken = refreshTokenService.getRefreshToken(user, false);
        Cookie refreshTokenCookie = refreshTokenService.getRefreshTokenCookie(refreshToken);
        String accessToken = jwtTokenService.getJwsToken(authResult.getAuthorities(), authResult.getName());

        return new TokenResponse(accessToken, refreshTokenCookie);
   }

   public TokenResponse refreshTokens(String suppliedRefreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenService.validateToken(suppliedRefreshToken);
        CustomUserDetails userDetails = userMapper.toCustomUserDetails(refreshTokenEntity.getUser());
        String accessToken = jwtTokenService.getJwsToken(userDetails.getAuthorities(), userDetails.getUsername());
        String refreshToken = refreshTokenService.getRefreshToken(refreshTokenEntity.getUser(), true);
        Cookie refreshTokenCookie = refreshTokenService.getRefreshTokenCookie(refreshToken);

        return new TokenResponse(accessToken, refreshTokenCookie);
   }


}
