package com.notic.advice;

import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.EntityAlreadyExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler({EntityDoesNotExistsException.class, EntityAlreadyExistsException.class})
    private ResponseEntity<?> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
