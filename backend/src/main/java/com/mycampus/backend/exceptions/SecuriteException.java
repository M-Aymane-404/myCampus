package com.mycampus.backend.exceptions;

public class SecuriteException extends RuntimeException {
    
    public SecuriteException(String message) {
        super(message);
    }
    
    public SecuriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
