package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.dto.SignInDto;
import com.notic.dto.TokenResponse;
import com.notic.dto.UserDto;
import com.notic.dto.RefreshTokenValidationResultDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.AuthenticationFlowException;
import com.notic.mapper.UserMapper;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;


    public UserDto signUp(CreateUserDto body) {
       User createdUser = userService.createUser(body);

       emailService.sendSimpleEmail(createdUser.getEmail(), "Email verification", "123456");

       return userMapper.toDto(createdUser);
   }

   public TokenResponse signIn(SignInDto body) {
        Authentication authReq = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        authenticationManager.authenticate(authReq);
        User user = userService.getUserByEmailWithRoles(body.email())
                .orElseThrow(() -> new AuthenticationFlowException("Authentication failed"));
        String refreshToken = refreshTokenService.getRefreshToken(user);
        Cookie refreshTokenCookie = refreshTokenService.getRefreshTokenCookie(refreshToken);
        String accessToken = jwtTokenService.getJwsToken(mapRoles(user.getRoles()), user.getId());

        return new TokenResponse(accessToken, refreshTokenCookie);
   }

   public TokenResponse refreshTokens(String refreshToken) {
        RefreshTokenValidationResultDto refreshTokenValidationResultDto = refreshTokenService.validateAndRotateToken(refreshToken);
        User user = refreshTokenValidationResultDto.refreshToken().getUser();
        String rawToken = refreshTokenValidationResultDto.rawRefreshToken();
        String accessToken = jwtTokenService.getJwsToken(mapRoles(user.getRoles()), user.getId());
        Cookie refreshTokenCookie = refreshTokenService.getRefreshTokenCookie(rawToken);

        return new TokenResponse(accessToken, refreshTokenCookie);
   }

   public void logout(String refreshToken) {
        refreshTokenService.deleteTokenByToken(refreshToken);
   }

   private Set<String> mapRoles(Set<Role> userRoles) {
       return userRoles.stream().map(Role::getName).collect(Collectors.toSet());
   }
}
