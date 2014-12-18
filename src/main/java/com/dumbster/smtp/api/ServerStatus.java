package com.dumbster.smtp.api;

public enum ServerStatus {
    STOPPED(404, "STOPPED"),
    RUNNING(200, "RUNNING");

    private String statusString;
    private int status;

    private ServerStatus(int status, String statusString) {
        this.status = status;
        this.statusString = statusString;
    }

    public String getStatusString() {
        return this.statusString;
    }

    public int getStatus() {
        return this.status;
    }
}
