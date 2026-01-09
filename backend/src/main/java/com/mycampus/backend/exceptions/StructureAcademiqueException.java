package com.mycampus.backend.exceptions;

public class StructureAcademiqueException extends Exception {
    
    public StructureAcademiqueException(String message) {
        super(message);
    }
    
    public StructureAcademiqueException(String message, Throwable cause) {
        super(message, cause);
    }
}
