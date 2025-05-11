package com.notic.controller.advice;

import com.notic.controller.FriendshipController;
import com.notic.controller.FriendshipRequestController;
import com.notic.exception.FriendshipException;
import com.notic.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {FriendshipController.class, FriendshipRequestController.class})
public class FriendshipControllerAdvice {

    @ExceptionHandler(FriendshipException.class)
    public ResponseEntity<ApiErrorResponse> handleFriendshipException(FriendshipException e) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
