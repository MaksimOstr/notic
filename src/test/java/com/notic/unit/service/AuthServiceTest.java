package com.notic.unit.service;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.dto.*;
import com.notic.entity.RefreshToken;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.entity.VerificationCode;
import com.notic.event.EmailVerificationEvent;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.TokenValidationException;
import com.notic.mapper.UserMapper;
import com.notic.service.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    private VerificationCodeService verificationCodeService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;

    @Captor
    private ArgumentCaptor<EmailVerificationEvent> emailVerificationEventCaptor;

    @Captor
    private ArgumentCaptor<Set<String>> rolesCaptor;

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

            verify(userService).createUser(createUserDto);

            assertEquals(errorMsg, result.getMessage());
        }

        @Test
        void SuccessfullySignUp() {
            VerificationCode verificationCode = new VerificationCode(user, 123456, Instant.now());

            when(verificationCodeService.createVerificationCode(any(User.class))).thenReturn(verificationCode);
            when(userService.createUser(any(CreateUserDto.class))).thenReturn(user);
            when(userMapper.toDto(any(User.class))).thenReturn(new UserDto(1, createUserDto.email(), createUserDto.username()));
            UserDto result = authService.signUp(createUserDto);


            verify(userService).createUser(createUserDto);
            verify(userMapper).toDto(user);
            verify(applicationEventPublisher).publishEvent(emailVerificationEventCaptor.capture());

            assertEquals(emailVerificationEventCaptor.getValue().code(), verificationCode.getCode());
            assertEquals(emailVerificationEventCaptor.getValue().email(), user.getEmail());
            assertNotNull(result);
            assertEquals(result.username(), createUserDto.username());
            assertEquals(result.email(), createUserDto.email());
        }
    }


    @Nested
    class SignIn {

        private final SignInDto signInDto = new SignInDto("test@gmail.com", "12121212");
        private final Role role = new Role("ROLE_USER");
        private final Set<Role> roles = Set.of(role);
        private final User user = new User(
                "Bob",
                signInDto.email(),
                null,
                roles
        );
        private final CustomUserDetails customUserDetails = new CustomUserDetails(user);
        private final Authentication authRes = new UsernamePasswordAuthenticationToken(customUserDetails, signInDto.password(), Set.of());


        @Test
        void InvalidCredentials() {
            when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new UsernameNotFoundException(""));

            assertThrows(AuthenticationException.class, () -> authService.signIn(signInDto));

            verify(authenticationManager).authenticate(authenticationCaptor.capture());
            verifyNoInteractions(jwtService, refreshTokenService, userMapper);

            assertFalse(authenticationCaptor.getValue().isAuthenticated());
            assertEquals(signInDto.email(), authenticationCaptor.getValue().getPrincipal());
            assertEquals(signInDto.password(), authenticationCaptor.getValue().getCredentials());
        }


        @Test
        void SuccessfullySignIn() {
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";

            when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authRes);
            when(refreshTokenService.getRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(userService.getUserById(anyLong())).thenReturn(Optional.of(user));
            when(jwtService.getJwsToken(any(), anyLong())).thenReturn(accessToken);

            TokenResponse result = authService.signIn(signInDto);

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(refreshTokenService).getRefreshToken(user);
            verify(jwtService).getJwsToken(rolesCaptor.capture(), anyLong());

            assertNotNull(result);
            assertEquals(accessToken, result.accessToken());
            Set<String> roles = rolesCaptor.getValue();
            Set<String> expectedRoles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            assertEquals(expectedRoles, roles);
        }

    }

    @Nested
    class RefreshTokenTest {
        private final String refreshToken = "refreshToken";


        @Test
        void InvalidRefreshToken() {
            when(refreshTokenService.validateAndRotateToken(anyString())).thenThrow(new TokenValidationException("Refresh token invalid"));

            assertThrows(TokenValidationException.class, () -> authService.refreshTokens(refreshToken));

            verify(refreshTokenService).validateAndRotateToken(refreshToken);
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

            when(refreshTokenService.validateAndRotateToken(anyString())).thenReturn(refreshTokenValidationResultDto);
            when(jwtService.getJwsToken(any(), anyLong())).thenReturn(accessToken);

            TokenResponse result = authService.refreshTokens(refreshToken);

            verify(refreshTokenService).validateAndRotateToken(refreshToken);
            verify(jwtService).getJwsToken(Set.of(userRole.getName()), user.getId());

            assertNotNull(result);
            assertEquals(accessToken, result.accessToken());
        }
    }


    @Test
    void logoutTest() {
        String refreshToken = "refreshToken";

        authService.logout(refreshToken);

        verify(refreshTokenService).deleteTokenByToken(refreshToken);
    }


    @Test
    void verifyAccount() {
        long code = 1123123L;
        long userId = 1L;

        when(verificationCodeService.verifyCode(anyLong())).thenReturn(userId);

        authService.verifyAccount(code);

        verify(verificationCodeService).verifyCode(code);
        verify(userService).markUserAsVerified(userId);

    }
}
