package com.github.saksham.hulaki.transport;

public class SmtpResult {
    private final SmtpState nextState;
    private final SmtpResponse smtpResponse;

    public SmtpResult(SmtpState nextState) {
        this(nextState, null);
    }

    public SmtpResult(SmtpState nextState, int responseCode, String message) {
        this(nextState, new SmtpResponse(responseCode, message));
    }

    public SmtpResult(SmtpState nextState, SmtpResponse smtpResponse) {
        this.nextState = nextState;
        this.smtpResponse = smtpResponse;
    }

    public SmtpResponse getSmtpResponse() {
        return smtpResponse;
    }

    public SmtpState getNextState() {
        return nextState;
    }

    public String toSmtpResponseString() {
        return (smtpResponse != null) ? smtpResponse.getResponseString() : "";
    }
}
