package com.dumbster.smtp.api;


public class InvalidRequest extends ApiRequest {

    public InvalidRequest() {
        super(ApiCommand.INVALID);
    }

    public InvalidRequest(String request) {
        super(request, ApiCommand.INVALID);
    }

    @Override
    public String toRequestString() {
        return ApiCommand.INVALID.toString();
    }
}
