package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiProtocolException;

public class ClearRequest extends ApiRequest {

    private String recipient;

    public ClearRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.CLEAR);
        String[] tokens = requestBody.split(" ");
        if (tokens.length != 2) {
            throw new ApiProtocolException("The count request should be in format COUNT <email-address>");
        }
        this.recipient = tokens[1];
    }

    public ClearRequest() {
        super(ApiCommand.CLEAR);
    }

    public String getRecipient() {
        return recipient;
    }

    public ClearRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String toRequestString() {
        return getCommand() + " " + getRecipient();
    }
}
