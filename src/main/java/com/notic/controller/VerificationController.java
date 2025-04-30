package com.notic.controller;

import com.notic.dto.request.SendVerificationCodeRequestDto;
import com.notic.dto.request.VerificationCodeRequestDto;
import com.notic.dto.response.ApiErrorResponse;
import com.notic.service.VerificationService;
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
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendVerification(@RequestBody SendVerificationCodeRequestDto body) {
        verificationService.createAndSendVerificationCode(body.email());
        String response = "Verification code sent to " + body.email();
        return ResponseEntity.ok(response);
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
    @PostMapping
    public ResponseEntity<String> verifyAccount(
            @Valid
            @RequestBody
            VerificationCodeRequestDto body
    ) {
            int parsedCode = Integer.parseInt(body.code());
            verificationService.verifyCode(parsedCode);
            String response = "Email successfully verified";
            return ResponseEntity.ok(response);
    }
}
