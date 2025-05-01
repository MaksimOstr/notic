package com.notic.advice;

import com.notic.controller.EmailVerificationController;
import com.notic.controller.ForgotPasswordController;
import com.notic.dto.response.ApiErrorResponse;
import com.notic.exception.ForgotPasswordException;
import com.notic.exception.VerificationCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {EmailVerificationController.class, ForgotPasswordController.class})
public class EmailAndForgotPasswordControllerAdvice {

    @ExceptionHandler({VerificationCodeException.class, ForgotPasswordException.class})
    public ResponseEntity<ApiErrorResponse> handleVerificationCodeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
