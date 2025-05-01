package com.notic.advice;

import com.notic.dto.response.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestControllerAdvice
public class AwsExceptionHandler {


    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ApiErrorResponse> handleS3Exception(S3Exception ex) {

        String errorCode = ex.awsErrorDetails().errorCode();
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.valueOf(ex.statusCode());

        ApiErrorResponse error = new ApiErrorResponse(errorCode, message, status.value());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(AwsServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAwsServiceException(AwsServiceException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                "AWS_SERVICE_ERROR",
                "AWS error: " + ex.getMessage(),
                ex.statusCode()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<ApiErrorResponse> handleSdkClientException(SdkClientException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                "CLIENT_ERROR",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
