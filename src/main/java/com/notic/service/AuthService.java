package com.notic.service;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.dto.CreateUserDto;
import com.notic.dto.SignInDto;
import com.notic.dto.TokenResponse;
import com.notic.dto.UserDto;
import com.notic.dto.RefreshTokenValidationResultDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.event.EmailVerificationEvent;
import com.notic.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final VerificationCodeService verificationCodeService;
    private final ApplicationEventPublisher applicationEventPublisher;


    public UserDto signUp(CreateUserDto body) {
        User createdUser = userService.createUser(body);
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(createdUser);
        applicationEventPublisher.publishEvent(new EmailVerificationEvent(
                createdUser.getEmail(), verificationCode.getCode()
        ));
        return userMapper.toDto(createdUser);
   }

   @Transactional
   public TokenResponse signIn(SignInDto body) {
        Authentication authReq = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        Authentication authentication = authenticationManager.authenticate(authReq);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.getUser();
        String refreshToken = refreshTokenService.getRefreshToken(user);
        String accessToken = jwtTokenService.getJwsToken(mapRoles(user.getRoles()), user.getId());

        return new TokenResponse(accessToken, refreshToken);
   }

   public TokenResponse refreshTokens(String refreshToken) {
        RefreshTokenValidationResultDto refreshTokenValidationResultDto = refreshTokenService.validateAndRotateToken(refreshToken);
        User user = refreshTokenValidationResultDto.refreshToken().getUser();
        String rawRefreshToken = refreshTokenValidationResultDto.rawRefreshToken();
        String accessToken = jwtTokenService.getJwsToken(mapRoles(user.getRoles()), user.getId());

        return new TokenResponse(accessToken, rawRefreshToken);
   }

   public void logout(String refreshToken) {
        refreshTokenService.deleteTokenByToken(refreshToken);
   }

   public void verifyAccount(long code) {
       long userId = verificationCodeService.verifyCode(code);

       userService.markUserAsVerified(userId);
   }

   private Set<String> mapRoles(Set<Role> userRoles) {
       return userRoles.stream().map(Role::getName).collect(Collectors.toSet());
   }
}
