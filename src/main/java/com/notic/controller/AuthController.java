package com.notic.controller;

import com.notic.constants.TokenConstants;
import com.notic.dto.CreateUserDto;
import com.notic.dto.SignInDto;
import com.notic.dto.TokenResponse;
import com.notic.dto.UserDto;
import com.notic.exception.TokenValidationException;
import com.notic.security.model.CustomUserDetails;
import com.notic.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody CreateUserDto body) {
        UserDto user = authService.signUp(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(
            @Valid @RequestBody SignInDto body,
            HttpServletResponse response
    ) {
        TokenResponse tokens = authService.signIn(body);
        response.addCookie(tokens.refreshTokenCookie());
        return ResponseEntity.status(HttpStatus.OK).body(tokens.accessToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshTokens(
            @CookieValue(value = TokenConstants.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if(refreshToken == null) {
            throw new TokenValidationException("Token was not provided.");
        }

        TokenResponse tokens = authService.refreshTokens(refreshToken);

        response.addCookie(tokens.refreshTokenCookie());
        return ResponseEntity.status(HttpStatus.OK).body(tokens.accessToken());
    }
}
