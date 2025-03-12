package com.notic.controller;

import com.notic.dto.CreateUserDto;
import com.notic.dto.UserDto;
import com.notic.service.AuthService;
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

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody CreateUserDto body) {
        UserDto user = authService.signUp(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
