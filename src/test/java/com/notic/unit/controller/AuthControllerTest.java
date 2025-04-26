package com.notic.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notic.advice.AuthControllerAdvice;
import com.notic.advice.GlobalControllerAdvice;
import com.notic.controller.AuthController;
import com.notic.dto.*;
import com.notic.dto.request.SignInRequestDto;
import com.notic.dto.request.VerificationCodeRequestDto;
import com.notic.dto.response.TokenResponse;
import com.notic.exception.*;
import com.notic.mapper.UserMapper;
import com.notic.config.security.handler.CustomAuthenticationEntryPoint;
import com.notic.service.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = {AuthController.class})
public class AuthControllerTest {

    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    @MockitoBean
    private VerificationCodeService verificationCodeService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        AuthController authController = new AuthController(authService, cookieService);
        GlobalControllerAdvice globalExceptionHandler = new GlobalControllerAdvice();
        AuthControllerAdvice authControllerAdvice = new AuthControllerAdvice();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(globalExceptionHandler, authControllerAdvice)
                .build();
    }

    @Nested
    class SignUpTest {

        private final CreateUserDto createUserDto = new CreateUserDto(
                "test@gmail.com",
                "test",
                "12121212"
        );

        @Test
        void shouldThrowErrorWithEmptyFields() throws Exception {
            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.username").isNotEmpty())
                    .andExpect(jsonPath("$.password").isNotEmpty())
                    .andExpect(jsonPath("$.email").isNotEmpty());


            verify(authService, never()).signUp(any());
        }

        @Test
        void shouldCreateAndReturnUser() throws Exception {

            when(authService.signUp(any(CreateUserDto.class))).thenReturn(new UserDto(1, createUserDto.email(), createUserDto.username()));

            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value(createUserDto.username()))
                    .andExpect(jsonPath("$.email").value(createUserDto.email()))
                    .andExpect(jsonPath("$.id").value(1));

            verify(authService).signUp(createUserDto);
        }

        @Test
        void userAlreadyExists() throws Exception {
            String errorMessage = "Such user already exists";

            when(authService.signUp(any(CreateUserDto.class))).thenThrow(new EntityAlreadyExistsException(errorMessage));

            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().is(409))
                    .andExpect(jsonPath("$.message").value(errorMessage));
            verify(authService).signUp(createUserDto);
        }
    }

    @Nested
    class SignInTest {

        private final SignInRequestDto signInDto = new SignInRequestDto("test@gmail.com", "12121212");

        @Test
        void shouldThrowErrorWithEmptyFields() throws Exception {
            mockMvc.perform(post("/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.password").isNotEmpty())
                    .andExpect(jsonPath("$.email").isNotEmpty());


            verifyNoInteractions(authService, cookieService);

        }

        @Test
        void signInSuccess() throws Exception {

            String refreshToken = "refreshTokenData";
            String accessToken = "accessToken";
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

            when(authService.signIn(any(SignInRequestDto.class))).thenReturn(new TokenResponse(accessToken, refreshToken));
            when(cookieService.createRefreshTokenCookie(anyString())).thenReturn(refreshTokenCookie);

            mockMvc.perform(post("/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signInDto)))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", "refreshToken="+refreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(accessToken));

            verify(authService).signIn(signInDto);
            verify(cookieService).createRefreshTokenCookie(refreshToken);
        }

        @Nested
        class RefreshTokenTest {

            private final String refreshToken = "refreshToken";
            private final Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

            @Test
            void tokenWasNotProvided() throws Exception {
                mockMvc.perform(post("/auth/refresh"))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message").value("Token was not provided"))
                        .andExpect(header().doesNotExist("Set-Cookie"));

                verify(authService, never()).refreshTokens(any());
            }

            @Test
            void successfulRefreshingTokens() throws Exception {
                String accessToken = "accessToken";
                TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);

                when(authService.refreshTokens(anyString())).thenReturn(tokenResponse);
                when(cookieService.createRefreshTokenCookie(anyString())).thenReturn(refreshCookie);

                mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshCookie))
                        .andExpect(status().isOk())
                        .andExpect(header().exists("Set-Cookie"))
                        .andExpect(cookie().value("refreshToken", refreshToken))
                        .andExpect(jsonPath("$").value(accessToken));

                verify(authService).refreshTokens(refreshToken);
                verify(cookieService).createRefreshTokenCookie(refreshToken);
            }

            @Test
            void tokenValidationError() throws Exception {

                String errorMessage = "Token was not provided";

                when(authService.refreshTokens(anyString())).thenThrow(new TokenValidationException(errorMessage));

                mockMvc.perform(post("/auth/refresh")
                                .cookie(refreshCookie))
                                .andExpect(status().isUnauthorized())
                                .andExpect(header().doesNotExist("Set-Cookie"))
                                .andExpect(jsonPath("$.message").value(errorMessage));

                verify(authService).refreshTokens(refreshToken);
                verify(cookieService, never()).createRefreshTokenCookie(anyString());
            }
        }
    }

    @Nested
    class LogoutTest {
        @Test
        void tokenWasNotProvided() throws Exception {
            String errorMessage = "Required data was not provided";

            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andExpect(header().doesNotExist("Set-Cookie"));

            verify(authService, never()).logout(any());
        }

        @Test
        void successfulLogout() throws Exception {

            String refreshToken = "refreshToken";
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

            doNothing().when(authService).logout(any());
            when(cookieService.deleteRefreshTokenCookie()).thenReturn(refreshCookie);


            mockMvc.perform(post("/auth/logout")
                    .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"));

            verify(authService).logout(refreshToken);
            verify(cookieService).deleteRefreshTokenCookie();
        }
    }


    @Nested
    class VerifyAccountTest {

        private final String code = "12345678";
        private final VerificationCodeRequestDto verificationCodeRequestDto = new VerificationCodeRequestDto(code);

        @Test
        void codeValidationError() throws Exception {
            String invalidCode = "1234";
            mockMvc.perform(post("/auth/verify-account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new VerificationCodeRequestDto(invalidCode))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").isNotEmpty());

            verify(authService, never()).verifyAccount(anyLong());
        }

        @Test
        void emptyFieldCode() throws Exception {
            mockMvc.perform(post("/auth/verify-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").isNotEmpty());

            verify(authService, never()).verifyAccount(anyLong());
        }

        @Test
        void successfulVerifyAccount() throws Exception {
            mockMvc.perform(post("/auth/verify-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isNotEmpty());

            verify(authService).verifyAccount(Long.parseLong(verificationCodeRequestDto.code()));
        }
    }
}