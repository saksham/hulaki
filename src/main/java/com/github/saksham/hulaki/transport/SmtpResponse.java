package com.github.saksham.hulaki.transport;


/**
 * SMTP response container.
 *
 * See RFC-2821 for details: https://www.ietf.org/rfc/rfc2821.txt
 */
public class SmtpResponse {

    private final int responseCode;
    private final String message;

    public SmtpResponse(int responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseString() {
        return responseCode + " " + message;
    }

    @Override
    public String toString() {
        return getResponseString();
    }
}
