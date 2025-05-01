package com.notic.controller;

import com.notic.dto.request.SignInRequestDto;
import com.notic.dto.request.SignUpRequestDto;
import com.notic.dto.response.SignUpResponseDto;
import com.notic.dto.response.TokenResponse;
import com.notic.exception.InvalidLogoutRequestException;
import com.notic.exception.TokenValidationException;
import com.notic.dto.response.ApiErrorResponse;
import com.notic.service.AuthService;
import com.notic.service.CookieService;
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
    private final CookieService cookieService;


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
                            examples = {@ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"User already exists\",\t\"status\": 409}")}
                    )

            )
    })
    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto body) {
        SignUpResponseDto user = authService.signUp(body);
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
                            examples = @ExampleObject("{\"code\": \"Unauthorized\",\t\"message\": \"Bad credentials\", \t\"status\": 401}")
                    )
            )
    })
    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(
            @Valid @RequestBody SignInRequestDto body,
            HttpServletResponse response
    ) {
            TokenResponse tokens = authService.signIn(body);
            Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(tokens.refreshToken());

            response.addCookie(refreshTokenCookie);
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
                            examples = @ExampleObject("{\"code\": \"Unauthorized\",\t\"message\": \"Token is invalid\", \t\"status\": 401}")
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
        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(tokens.refreshToken());

        response.addCookie(refreshTokenCookie);
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
                            examples = @ExampleObject("{\"code\": \"Bad request\",\t\"message\": \"Logout error\", \t\"status\": 400}")
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
        Cookie refreshTokenCookie = cookieService.deleteRefreshTokenCookie();

        response.addCookie(refreshTokenCookie);
        return ResponseEntity.status(HttpStatus.OK).body("You have been logged out successfully");
    }
}
