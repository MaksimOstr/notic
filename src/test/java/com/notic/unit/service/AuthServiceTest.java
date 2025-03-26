package com.notic.unit.service;

import com.notic.dto.*;
import com.notic.entity.RefreshToken;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.TokenValidationException;
import com.notic.mapper.UserMapper;
import com.notic.service.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {


    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;


    @Nested
    class SignUp {
        private final CreateUserDto createUserDto = new CreateUserDto("test@gmail.com", "test", "12121212");
        private final  User user = new User(createUserDto.username(), createUserDto.email(), "hashed", Set.of());

        @Test
        void UserAlreadyExists() {
            String errorMsg = "User already exists";
            CreateUserDto createUserDto = new CreateUserDto("test@gmail.com", "test", "12121212");


            when(userService.createUser(any(CreateUserDto.class))).thenThrow(new EntityAlreadyExistsException(errorMsg));

            Exception result = assertThrows(EntityAlreadyExistsException.class, () -> authService.signUp(createUserDto));

            verify(userService, times(1)).createUser(createUserDto);

            assertEquals(errorMsg, result.getMessage());
        }

        @Test
        void SuccesfullySignUp() {
            when(userService.createUser(any(CreateUserDto.class))).thenReturn(user);
            when(userMapper.toDto(any(User.class))).thenReturn(new UserDto(1, createUserDto.email(), createUserDto.username()));

            UserDto result = authService.signUp(createUserDto);


            verify(userService, times(1)).createUser(createUserDto);
            verify(userMapper, times(1)).toDto(user);


            assertNotNull(result);
            assertEquals(result.username(), createUserDto.username());
            assertEquals(result.email(), createUserDto.email());
        }
    }


    @Nested
    class SignIn {
        private final SignInDto signInDto = new SignInDto("test@gmail.com", "12121212");


        @Test
        void InvalidCredentials() {
            when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new UsernameNotFoundException(""));


            Exception result  = assertThrows(AuthenticationException.class, () -> authService.signIn(signInDto));

            verify(authenticationManager, times(1)).authenticate(authenticationCaptor.capture());
            verifyNoInteractions(jwtService, refreshTokenService, userMapper, userService);

            assertFalse(authenticationCaptor.getValue().isAuthenticated());
            assertEquals(signInDto.email(), authenticationCaptor.getValue().getPrincipal());
            assertEquals(signInDto.password(), authenticationCaptor.getValue().getCredentials());
        }


        @Test
        void SuccessfullySignIn() {

            Authentication authRes = new UsernamePasswordAuthenticationToken(signInDto.email(), signInDto.password(), Set.of());
            User user = new User("test", signInDto.email(), "hashed", Set.of());
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

            when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authRes);
            when(userService.getUserByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
            when(refreshTokenService.getRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(refreshTokenService.getRefreshTokenCookie(anyString())).thenReturn(refreshTokenCookie);
            when(jwtService.getJwsToken(any(), anyLong())).thenReturn(accessToken);

            TokenResponse result = authService.signIn(signInDto);

            verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
            verify(userService, times(1)).getUserByEmailWithRoles(signInDto.email());
            verify(refreshTokenService, times(1)).getRefreshToken(user);
            verify(refreshTokenService, times(1)).getRefreshTokenCookie(refreshToken);
            verify(jwtService, times(1)).getJwsToken(any(), anyLong());

            assertNotNull(result);
            assertEquals(accessToken, result.accessToken());
            assertEquals(refreshTokenCookie, result.refreshTokenCookie());
        }

    }

    @Nested
    class RefreshTokenTest {
        private final String refreshToken = "refreshToken";


        @Test
        void InvalidRefreshToken() {
            when(refreshTokenService.validateAndRotateToken(anyString())).thenThrow(new TokenValidationException("Refresh token invalid"));

            assertThrows(TokenValidationException.class, () -> authService.refreshTokens(refreshToken));

            verify(refreshTokenService, times(1)).validateAndRotateToken(refreshToken);
            verifyNoMoreInteractions(jwtService, refreshTokenService);
        }

        @Test
        void successfullyRefreshToken() {
            String hashedRefreshToken = "hashedRefreshToken";
            Role userRole = new Role("USER");
            User user = new User("test", "test@gmail.com", hashedRefreshToken, Set.of(userRole));
            RefreshToken refreshTokenEntity = new RefreshToken(refreshToken, user, Instant.now());
            RefreshTokenValidationResultDto refreshTokenValidationResultDto = new RefreshTokenValidationResultDto(refreshTokenEntity, refreshToken);
            String accessToken = "accessToken";
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

            when(refreshTokenService.validateAndRotateToken(anyString())).thenReturn(refreshTokenValidationResultDto);
            when(jwtService.getJwsToken(any(), anyLong())).thenReturn(accessToken);
            when(refreshTokenService.getRefreshTokenCookie(refreshToken)).thenReturn(refreshTokenCookie);

            TokenResponse result = authService.refreshTokens(refreshToken);

            verify(refreshTokenService, times(1)).validateAndRotateToken(refreshToken);
            verify(jwtService, times(1)).getJwsToken(Set.of(userRole.getName()), user.getId());
            verify(refreshTokenService, times(1)).getRefreshTokenCookie(refreshToken);

            assertNotNull(result);
            assertEquals(accessToken, result.accessToken());
            assertEquals(refreshTokenCookie, result.refreshTokenCookie());
        }
    }

    @Test
    void logoutTest() {
        String refreshToken = "refreshToken";

        authService.logout(refreshToken);

        verify(refreshTokenService, times(1)).deleteTokenByToken(refreshToken);
    }
}
