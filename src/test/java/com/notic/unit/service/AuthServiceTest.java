package com.notic.unit.service;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.dto.*;
import com.notic.dto.request.SignInRequestDto;
import com.notic.dto.request.SignUpRequestDto;
import com.notic.dto.response.SignUpResponseDto;
import com.notic.dto.response.TokenResponse;
import com.notic.entity.*;
import com.notic.event.UserCreationEvent;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.mapper.AuthMapper;
import com.notic.projection.UserWithRolesProjection;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    AuthMapper authMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;

    @Captor
    private ArgumentCaptor<UserCreationEvent> userCreationEventCaptor;

    @Captor
    private ArgumentCaptor<Set<String>> rolesCaptor;

    @Nested
    class SignUp {
        private final String password = "<PASSWORD>";
        private final String hashedPassword = "hashedPassword";
        private final  SignUpRequestDto signUpRequestDto = new SignUpRequestDto("test@gmail.com", "test", password);
        private final  User user = new User(signUpRequestDto.email(), hashedPassword, Set.of());
        private final Profile profile = new Profile(
                signUpRequestDto.username(),
                null,
                user
        );
        private final UserWithProfileDto userWithProfileDto = new UserWithProfileDto(user, profile);
        private final CreateLocalUserDto createLocalUserDto = new CreateLocalUserDto(signUpRequestDto.email(), password, signUpRequestDto.email());

        @Test
        void UserAlreadyExists() {
            when(authMapper.signUptoCreateUserDto(any(SignUpRequestDto.class))).thenReturn(createLocalUserDto);
            when(userService.createUser(any(CreateLocalUserDto.class))).thenThrow(new EntityAlreadyExistsException(anyString()));

            assertThrows(EntityAlreadyExistsException.class, () -> authService.signUp(signUpRequestDto));

            verify(userService).createUser(createLocalUserDto);
            verify(authMapper).signUptoCreateUserDto(signUpRequestDto);
            verifyNoInteractions(applicationEventPublisher);
        }

        @Test
        void SuccessfullySignUp() {
            when(authMapper.signUptoCreateUserDto(any(SignUpRequestDto.class))).thenReturn(createLocalUserDto);
            when(userService.createUser(any(CreateLocalUserDto.class))).thenReturn(userWithProfileDto);
            SignUpResponseDto result = authService.signUp(signUpRequestDto);


            verify(userService).createUser(createLocalUserDto);
            verify(applicationEventPublisher).publishEvent(userCreationEventCaptor.capture());

            assertEquals(userCreationEventCaptor.getValue().email(), user.getEmail());
            assertNotNull(result);
            assertEquals(signUpRequestDto.username(), result.getUsername());
            assertEquals(signUpRequestDto.email(), result.getEmail());
        }
    }


    @Nested
    class SignIn {

        private final SignInRequestDto signInDto = new SignInRequestDto("test@gmail.com", "12121212");
        private final Role role = new Role("ROLE_USER");
        private final Set<Role> roles = Set.of(role);
        private final User user = new User(
                signInDto.email(),
                null,
                roles
        );
        private final UserWithRolesProjection projection = new UserWithRolesProjection(

        )
        private final CustomUserDetails customUserDetails = new CustomUserDetails(user);
        private final Authentication authRes = new UsernamePasswordAuthenticationToken(customUserDetails, signInDto.password(), Set.of());
        private final String refreshToken = "refreshToken";
        private final String accessToken = "accessToken";
        private final TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);


        @Test
        void InvalidCredentials() {
            when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new UsernameNotFoundException(""));

            assertThrows(AuthenticationException.class, () -> authService.signIn(signInDto));

            verify(authenticationManager).authenticate(authenticationCaptor.capture());

            assertFalse(authenticationCaptor.getValue().isAuthenticated());
            assertEquals(signInDto.email(), authenticationCaptor.getValue().getPrincipal());
            assertEquals(signInDto.password(), authenticationCaptor.getValue().getCredentials());
        }


        @Test
        void SuccessfullySignIn() {

            when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authRes);
            when(userService.getUserById(anyLong())).thenReturn(Optional.of(user));
            when(tokenService.getTokenPair(anyLong(), anySet())).thenReturn(tokenResponse);

            TokenResponse result = authService.signIn(signInDto);

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(tokenService).getTokenPair(anyLong(), rolesCaptor.capture());

            assertNotNull(result);
            assertEquals(accessToken, result.accessToken());
            Set<String> roles = rolesCaptor.getValue();
            Set<String> expectedRoles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            assertEquals(expectedRoles, roles);
        }

    }

    @Test
    void refreshTokens() {
        String newRefreshToken = "newRefreshToken";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        TokenResponse tokenResponse = new TokenResponse(accessToken, newRefreshToken);

        when(tokenService.refreshTokens(anyString())).thenReturn(tokenResponse);
        TokenResponse result = authService.refreshTokens(refreshToken);

        verify(tokenService).refreshTokens(refreshToken);
        assertNotNull(result);
        assertEquals(accessToken, result.accessToken());
        assertEquals(newRefreshToken, result.refreshToken());
    }


    @Test
    void logoutTest() {
        String refreshToken = "refreshToken";

        authService.logout(refreshToken);

        verify(refreshTokenService).deleteTokenByToken(refreshToken);
    }
}
