package com.restaurante.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String entity, Object id) {
        super(entity + " con id " + id + " no encontrado");
    }
}
