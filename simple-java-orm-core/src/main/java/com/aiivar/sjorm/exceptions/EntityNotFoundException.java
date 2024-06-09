package com.aiivar.sjorm.exceptions;

public class EntityNotFoundException extends OrmException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}