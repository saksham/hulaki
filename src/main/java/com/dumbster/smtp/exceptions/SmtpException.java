package com.dumbster.smtp.exceptions;


public class SmtpException extends RuntimeException {
    public SmtpException(String message) {
        super(message);
    }

    public SmtpException(String message, Exception cause) {
        super(message, cause);
    }
}
