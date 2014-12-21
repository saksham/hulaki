package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiProtocolException;
import org.apache.commons.lang3.StringUtils;

public class ServerStatusRequest extends ApiRequest {

    private final ServerName serverName;

    public ServerStatusRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.SERVER_STATUS);
        String[] tokens = requestBody.split(" ");
        if(tokens.length != 2) {
            throw new ApiProtocolException("Server status should be in format SERVER_STATUS [" + StringUtils.join(ServerName.values(), '|')+"]");
        }
        serverName = ServerName.parse(tokens[1].trim());
    }


    @Override
    public String toRequestString() {
        return ApiCommand.SERVER_STATUS.getCommand();
    }

    public ServerName getServerName() {
        return serverName;
    }
}
