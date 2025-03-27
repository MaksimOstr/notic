package com.notic.controller;

import com.notic.dto.*;
import com.notic.exception.InvalidLogoutRequestException;
import com.notic.exception.TokenValidationException;
import com.notic.response.ApiErrorResponse;
import com.notic.service.AuthService;
import com.notic.utils.RefreshTokenUtils;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = {@ExampleObject("{\"message\": \"User already exists\", \"status\": \"409\"}")}
                    )

            )
    })
    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody CreateUserDto body) {
        UserDto user = authService.signUp(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful sign in operation",
                    content = @Content(examples = @ExampleObject(value = "access_token")),
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Refresh token",
                            schema = @Schema(
                                    example = "refreshToken=1141i3jh3b14jb2435b2k34"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\"message\": \"Bad credentials\", \"status\": \"401\"}")
                    )
            )
    })
    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(
            @Valid @RequestBody SignInDto body,
            HttpServletResponse response
    ) {
            TokenResponse tokens = authService.signIn(body);
            response.addCookie(tokens.refreshTokenCookie());
            return ResponseEntity.status(HttpStatus.OK).body(tokens.accessToken());
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful refresh token operation",
                    content = @Content(examples = @ExampleObject(value = "access_token")),
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Refresh token",
                            schema = @Schema(
                                    example = "refreshToken=1141i3jh3b14jb2435b2k34"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token validation fail",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\"message\": \"Token validation fail\", \"status\": \"401\"}")
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshTokens(
            @CookieValue(value = RefreshTokenUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if(refreshToken == null) {
            throw new TokenValidationException("Token was not provided");
        }

        TokenResponse tokens = authService.refreshTokens(refreshToken);

        response.addCookie(tokens.refreshTokenCookie());
        return ResponseEntity.status(HttpStatus.OK).body(tokens.accessToken());
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful logout operation",
                    content = @Content(
                            examples = @ExampleObject(value = "You have been logged out successfully.")
                    ),
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Delete refresh token cookie",
                            schema = @Schema(
                                    example = "refreshToken=null"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token was not provided",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\"message\": \"Logout failed\", \"status\": \"400\"}")
                    )

            )
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = RefreshTokenUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if(refreshToken == null) {
            throw new InvalidLogoutRequestException("Required data was not provided");
        }

        authService.logout(refreshToken);
        Cookie refreshTokenCookie = new Cookie(RefreshTokenUtils.REFRESH_TOKEN_COOKIE_NAME, null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.status(HttpStatus.OK).body("You have been logged out successfully");
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Code is valid and account was activated",
                    content = @Content(
                            examples = @ExampleObject("Email verified")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Code is invalid or expired",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\"message\": \"Verification code is expired\", \"status\": \"400\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Code is valid, but user does not exist",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\"message\": \"User does not exist\", \"status\": \"409\"}")
                    )
            )
    })
    @PostMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(
            @Valid
            @RequestBody
            VerificationCodeRequestDto request
    ) {
        long parsedCode = Long.parseLong(request.code());

        authService.verifyAccount(parsedCode);

        return ResponseEntity.status(HttpStatus.OK).body("Email verified");
    }
}
