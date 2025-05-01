package com.notic.controller;

import com.notic.dto.request.RequestVerificationCodeDto;
import com.notic.dto.request.ResetPasswordRequestDto;
import com.notic.dto.response.ApiErrorResponse;
import com.notic.service.ForgotPasswordService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {
    private final ForgotPasswordService forgotPasswordService;


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification code was sent to your email"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If user who requests was not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = {@ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"User not found\",\t\"status\": 409}")}
                    )
            )
    })
    @PostMapping("/request")
    public ResponseEntity<String> forgotPassword(
            @RequestBody @Valid RequestVerificationCodeDto body
            ) {
        forgotPasswordService.requestPasswordReset(body.email());
        String response = "Password reset code was sent to " + body.email();
        return ResponseEntity.ok(response);
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Code is valid and password was changed",
                    content = @Content(
                            examples = @ExampleObject("Password was successfully changed")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Code is invalid or expired",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\"code\": \"Bad request\",\t\"message\": \"Verification code expired\", \t\"status\": 400}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Code is valid, but user does not exist",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"User already exists\",\t\"status\": 409}")
                    )
            )
    })
    @PostMapping("/reset")
    public ResponseEntity<String> resetUserPassword(
            @RequestBody @Valid ResetPasswordRequestDto body
    ) {
        int parsedCode = Integer.parseInt(body.code());
        forgotPasswordService.verifyCodeAndChangePassword(parsedCode, body.newPassword());
        String response = "Password was successfully changed";
        return ResponseEntity.ok(response);
    }
}
