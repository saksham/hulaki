package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiProtocolException;

public class SmtpServerStatusRequest extends ApiRequest {

    public SmtpServerStatusRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.SMTP_SERVER_STATUS);
    }


    @Override
    public String toRequestString() {
        return ApiCommand.SMTP_SERVER_STATUS.getCommand();
    }
}
