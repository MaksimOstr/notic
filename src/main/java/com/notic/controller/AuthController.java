package com.notic.controller;

import com.notic.dto.SignUpDto;
import com.notic.dto.UserDto;
import com.notic.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody SignUpDto body) {
        UserDto user = authService.signUp(body);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
