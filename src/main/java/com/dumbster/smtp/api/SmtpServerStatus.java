package com.dumbster.smtp.api;

import java.lang.String;public enum SmtpServerStatus {
    STOPPED(404, "STOPPED"),
    RUNNING(200, "RUNNING");

    private String statusString;
    private int status;

    private SmtpServerStatus(int status, String statusString) {
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
