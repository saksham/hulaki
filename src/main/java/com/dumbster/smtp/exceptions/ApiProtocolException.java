package com.dumbster.smtp.exceptions;

public class ApiProtocolException extends RuntimeException {

    public ApiProtocolException(String message) {
        super(message);
    }

    public ApiProtocolException(Exception ex) {
        super(ex);
    }

    public ApiProtocolException(String message, Exception ex) {
        super(message, ex);
    }
}
