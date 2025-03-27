package com.notic.exception;

import lombok.Getter;

@Getter
public class AwsException extends RuntimeException {
    int code;

    public AwsException(String message, int code) {
        super(message);
        this.code = code;
    }
}
