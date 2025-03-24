package com.notic.exception;

public class InvalidLogoutRequestException extends RuntimeException {
    public InvalidLogoutRequestException(String message) {
        super(message);
    }
}
