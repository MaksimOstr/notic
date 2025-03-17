package com.notic.controller;

import com.notic.dto.CreateUserDto;
import com.notic.dto.SignInDto;
import com.notic.dto.UserDto;
import com.notic.security.model.CustomUserDetails;
import com.notic.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;


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
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInDto body) {
        String token = authService.signIn(body);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(userDetails);
    }
}
