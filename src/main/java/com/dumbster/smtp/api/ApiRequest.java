package com.dumbster.smtp.api;


import com.dumbster.smtp.exceptions.ApiProtocolException;

public abstract class ApiRequest {
    private String requestBody;
    private ApiCommand command;

    public ApiRequest(String requestBody, ApiCommand command) throws ApiProtocolException {
        this.requestBody = requestBody;
        this.command = command;
    }

    public ApiRequest(ApiCommand command) {
        this.command = command;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public ApiCommand getCommand() {
        return command;
    }

    public abstract String toRequestString();

    public static ApiRequest fromRequestString(String requestBody) {
        if (requestBody == null) {
            throw new ApiProtocolException("Unrecognized command received.");
        }

        String[] tokens = requestBody.split(" ");
        String command = tokens[0];
        if (command.equalsIgnoreCase(ApiCommand.COUNT.getCommand())) {
            return new CountRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.GET.getCommand())) {
            return new GetRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.CLEAR.getCommand())) {
            return new ClearRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.RELAY.getCommand())) {
            return new RelayRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.SMTP_SERVER_STATUS.getCommand())) {
            return new SmtpServerStatusRequest(requestBody);
        } else {
            return new InvalidRequest(requestBody);
        }
    }
}
