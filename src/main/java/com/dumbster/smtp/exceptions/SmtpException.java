package com.dumbster.smtp.exceptions;


public class SmtpException extends RuntimeException {
    public SmtpException(String message) {
        super(message);
    }

    public SmtpException(Exception cause) {
        super(cause);
    }

    public SmtpException(String message, Exception cause) {
        super(message, cause);
    }
}
