package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiProtocolException;

public enum RelayMode {
    ADD("ADD"),
    REMOVE("REMOVE"),
    GET("GET");

    private String mode;

    RelayMode(String mode) {
        this.mode = mode;
    }


    @Override
    public String toString() {
        return this.mode;
    }

    public static RelayMode[] all() {
        return new RelayMode[] {ADD, REMOVE, GET};
    }

    public static RelayMode parse(String modeAsString) {
        for(RelayMode mode : all()) {
            if (mode.toString().equalsIgnoreCase(modeAsString)) {
                return mode;
            }
        }
        throw new ApiProtocolException("Could not parse the relay-mode: " + modeAsString);
    }
}
