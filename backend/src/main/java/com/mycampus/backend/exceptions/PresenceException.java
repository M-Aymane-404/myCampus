package com.mycampus.backend.exceptions;

public class PresenceException extends RuntimeException  {

    public PresenceException(String message) {
        super(message);
    }

    public PresenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
