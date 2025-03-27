package com.notic.advice;

import com.notic.controller.AuthController;
import com.notic.exception.AuthenticationFlowException;
import com.notic.exception.InvalidLogoutRequestException;
import com.notic.exception.TokenValidationException;
import com.notic.exception.VerificationCodeException;
import com.notic.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthControllerAdvice {

    @ExceptionHandler({AuthenticationFlowException.class, TokenValidationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(), e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(InvalidLogoutRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLogoutRequest(InvalidLogoutRequestException e) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(VerificationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleVerificationCodeException(VerificationCodeException e) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
