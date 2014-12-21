package com.dumbster.smtp.exceptions;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(Exception ex) {
        super(ex);
    }

    public ApiException(String message, Exception ex) {
        super(message, ex);
    }
}
