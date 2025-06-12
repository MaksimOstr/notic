package com.notic.enums;

import com.notic.exception.EntityDoesNotExistsException;

public enum AuthProviderEnum {
    LOCAL, GOOGLE;

    public static AuthProviderEnum fromString(String name) {
        try {
            String uppercaseName = name.toUpperCase();
            return AuthProviderEnum.valueOf(uppercaseName);
        } catch (IllegalArgumentException e) {
            throw new EntityDoesNotExistsException("Unsupported auth provider: " + name);
        }
    }
}
