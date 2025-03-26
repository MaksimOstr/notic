package com.notic.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema()
public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
