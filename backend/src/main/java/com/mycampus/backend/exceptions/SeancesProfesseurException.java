package com.mycampus.backend.exceptions;

public class SeancesProfesseurException extends RuntimeException {
    public SeancesProfesseurException(String message) {
        super(message);
    }

    public SeancesProfesseurException(String message, Throwable cause) {
        super(message, cause);
    }
}
