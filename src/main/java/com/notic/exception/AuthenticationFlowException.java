package com.notic.exception;

public class AuthenticationFlowException extends RuntimeException {
    public AuthenticationFlowException(String message) {
        super(message);
    }
}
