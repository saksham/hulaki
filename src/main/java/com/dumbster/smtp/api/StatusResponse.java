package com.dumbster.smtp.api;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="status-response", namespace = "http://wirecardbank.com/smtp")
public class StatusResponse extends ApiResponse {

    private int status;
    private String message;

    public StatusResponse() {} // Empty constructor for JAXB

    public StatusResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }


    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}