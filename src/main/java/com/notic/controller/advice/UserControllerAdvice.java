package com.notic.controller.advice;

import com.notic.controller.UserController;
import com.notic.exception.UploadFileException;
import com.notic.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerAdvice {

    @ExceptionHandler(UploadFileException.class)
    public ResponseEntity<ApiErrorResponse> handleUploadFileException(UploadFileException e) {
        return ResponseEntity.internalServerError().body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
