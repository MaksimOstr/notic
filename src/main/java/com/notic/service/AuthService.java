package com.notic.service;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.dto.*;
import com.notic.entity.Profile;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.event.EmailVerificationEvent;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.mapper.AuthMapper;
import com.notic.mapper.UserMapper;
import com.notic.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final ProfileService profileService;
    private final AuthMapper authMapper;


    public UserDto signUp(SignUpRequestDto dto) {
        CreateUserDto createUserDto = authMapper.toCreateUserDto(dto);
        User createdUser = userService.createUser(createUserDto);
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(createdUser);
        CreateProfileDto createProfileDto = new CreateProfileDto(
                dto.username(),
                null,
                createdUser
        );
        Profile profile = profileService.createProfile(createProfileDto);
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
        User user = userService.getUserById(customUserDetails.getUserId())
                .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));
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
