package com.dumbster.smtp.api;


public enum ApiCommand {
    COUNT("COUNT"),
    CLEAR("CLEAR"),
    GET("GET"),
    RELAY("RELAY"),
    INVALID("INVALID"),
    SERVER_STATUS("SERVER_STATUS");

    private final String command;

    ApiCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }
}
