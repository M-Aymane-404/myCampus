package com.mycampus.backend.exceptions;

public class UtilisateursException extends Exception {
    
    public UtilisateursException(String message) {
        super(message);
    }
    
    public UtilisateursException(String message, Throwable cause) {
        super(message, cause);
    }
}
