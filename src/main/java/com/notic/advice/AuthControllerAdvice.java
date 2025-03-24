package com.notic.advice;

import com.notic.controller.AuthController;
import com.notic.exception.AuthenticationFlowException;
import com.notic.exception.InvalidLogoutRequestException;
import com.notic.exception.TokenValidationException;
import com.notic.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthControllerAdvice {

    @ExceptionHandler({AuthenticationFlowException.class, TokenValidationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(e.getMessage(), "Unauthorized"));
    }

    @ExceptionHandler(InvalidLogoutRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLogoutRequest(InvalidLogoutRequestException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(ex.getMessage(), "Bad Request"));
    }
}
