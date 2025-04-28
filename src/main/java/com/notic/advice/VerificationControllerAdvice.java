package com.notic.advice;

import com.notic.controller.VerificationController;
import com.notic.dto.response.ApiErrorResponse;
import com.notic.exception.VerificationCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = VerificationController.class)
public class VerificationControllerAdvice {

    @ExceptionHandler(VerificationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleVerificationCodeException(VerificationCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
