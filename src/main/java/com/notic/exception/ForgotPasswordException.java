package com.notic.exception;

public class ForgotPasswordException extends RuntimeException {
    public ForgotPasswordException(String message) {
        super(message);
    }
}
