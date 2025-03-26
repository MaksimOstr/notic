package com.notic.exception;

public class EntityDoesNotExistsException extends RuntimeException {
    public EntityDoesNotExistsException(String message) {
        super(message);
    }
}
