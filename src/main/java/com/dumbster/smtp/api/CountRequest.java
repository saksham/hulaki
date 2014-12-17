package com.dumbster.smtp.api;


import com.dumbster.smtp.exceptions.ApiProtocolException;

public class CountRequest extends ApiRequest {

    private String recipient;

    public CountRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.COUNT);
        String[] tokens = requestBody.split(" ");
        if (tokens.length == 2) {
            this.recipient = tokens[1];
        } else if (tokens.length > 2) {
            throw new ApiProtocolException("The count request should be in format COUNT [<email-address>]");
        }

    }

    public CountRequest() {
        super(ApiCommand.COUNT);
    }

    public String getRecipient() {
        return recipient;
    }

    public CountRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String toRequestString() {
        if(recipient != null) {
            return getCommand().toString() + " " + recipient;
        } else {
            return getCommand().toString();
        }
    }
}
