package com.notic.advice;

import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler({EntityDoesNotExistsException.class, EntityAlreadyExistsException.class})
    private ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Map<String, String>> handleNotValidMethodArguments(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IOException.class)
    private ResponseEntity<ApiErrorResponse> handleFileProcessingException(IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

}
