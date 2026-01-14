package com.mycampus.backend.exceptions;

public class StructureAcademiqueException extends RuntimeException {
    
    public StructureAcademiqueException(String message) {
        super(message);
    }
    
    public StructureAcademiqueException(String message, Throwable cause) {
        super(message, cause);
    }
}
