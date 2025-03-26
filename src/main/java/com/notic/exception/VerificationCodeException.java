package com.notic.exception;

public class VerificationCodeException extends RuntimeException {
    public VerificationCodeException(String message) {
        super(message);
    }
}
