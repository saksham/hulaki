package com.dumbster.smtp.api;


import com.dumbster.smtp.exceptions.ApiProtocolException;
import org.apache.commons.lang3.StringUtils;

public class RelayRequest extends ApiRequest {

    private String recipient;
    private RelayMode relayMode;

    public RelayRequest() {
        super(ApiCommand.RELAY);
    }

    public RelayRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.RELAY);
        String[] tokens = requestBody.split(" ");
        if (tokens.length == 2) {
            this.relayMode = RelayMode.parse(tokens[1]);
        } else if (tokens.length == 3) {
            this.relayMode = RelayMode.parse(tokens[1]);
            this.recipient = tokens[2];
        } else {
            throw new ApiProtocolException("Invalid format. Should be in format: " + ApiCommand.RELAY.toString() + " [" +
                    StringUtils.join(RelayMode.all(), "|") + "] [email-address]");
        }
    }


    public String getRecipient() {
        return recipient;
    }

    public RelayRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public RelayMode getRelayMode() {
        return relayMode;
    }

    public RelayRequest setRelayMode(RelayMode relayMode) {
        this.relayMode = relayMode;
        return this;
    }


    @Override
    public String toRequestString() {
        StringBuilder request = new StringBuilder(ApiCommand.RELAY.toString());
        request.append(" ").append(this.relayMode);
        if(this.recipient != null) {
            request.append(" ").append(recipient);
        }
        return request.toString();
    }
}
