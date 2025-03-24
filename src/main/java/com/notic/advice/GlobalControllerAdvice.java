package com.notic.advice;

import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.response.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler({EntityDoesNotExistsException.class, EntityAlreadyExistsException.class})
    private ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(409).body(new ApiErrorResponse(ex.getMessage(), "Conflict"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Map<String, String>> handleNotValidMethodArguments(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
