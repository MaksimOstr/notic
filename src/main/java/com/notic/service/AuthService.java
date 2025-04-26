package com.notic.service;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.dto.*;
import com.notic.dto.request.SignInRequestDto;
import com.notic.dto.request.SignUpRequestDto;
import com.notic.dto.response.SignUpResponseDto;
import com.notic.dto.response.TokenResponse;
import com.notic.entity.User;
import com.notic.event.UserCreationEvent;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuthMapper authMapper;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto dto) {
        CreateLocalUserDto createUserDto = authMapper.signUptoCreateUserDto(dto);
        UserWithProfileDto userWithProfile = userService.createUser(createUserDto);

        User user = userWithProfile.user();

        applicationEventPublisher.publishEvent(new UserCreationEvent(
                user.getEmail()
        ));

        return new SignUpResponseDto(userWithProfile);
   }

   @Transactional
   public TokenResponse signIn(SignInRequestDto body) {
        Authentication authReq = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        Authentication authentication = authenticationManager.authenticate(authReq);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userService.getUserById(customUserDetails.getUserId())
                .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));
        return tokenService.getTokenPair(user);
   }

   public TokenResponse refreshTokens(String refreshToken) {
        return tokenService.refreshTokens(refreshToken);
   }

   public void logout(String refreshToken) {
        refreshTokenService.deleteTokenByToken(refreshToken);
   }
}
