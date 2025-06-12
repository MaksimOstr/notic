package com.notic.enums;

import com.notic.exception.EntityDoesNotExistsException;

public enum NoteVisibilityEnum {
    PUBLIC, PRIVATE, PROTECTED;

    public static NoteVisibilityEnum fromString(String name) {
        try {
            String uppercaseName = name.toUpperCase();
            return NoteVisibilityEnum.valueOf(uppercaseName);
        } catch (IllegalArgumentException e) {
            throw new EntityDoesNotExistsException("Unsupported note visibility: " + name);
        }
    }
}
