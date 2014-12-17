package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiProtocolException;

public class GetRequest extends ApiRequest {

    private String recipient;

    public GetRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.GET);
        String[] tokens = requestBody.split(" ");
        if (tokens.length != 2) {
            throw new ApiProtocolException("The count request should be in format COUNT <email-address>");
        }
        this.recipient = tokens[1];
    }

    public GetRequest() {
        super(ApiCommand.GET);
    }

    public String getRecipient() {
        return recipient;
    }

    public GetRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String toRequestString() {
        return getCommand() + " " + getRecipient();
    }
}
