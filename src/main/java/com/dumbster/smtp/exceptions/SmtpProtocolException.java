package com.dumbster.smtp.exceptions;

public class SmtpProtocolException extends SmtpException {

    public SmtpProtocolException(String message, Exception cause) {
        super(message, cause);
    }
}
