package com.dumbster.smtp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "count-response", namespace = "https://github.com/saksham/dumbster")
public class CountResponse extends ApiResponse {

    private String recipient;
    private int count;

    public CountResponse() {} // Empty constructor for JAXB

    public CountResponse(String recipient, int count) {
        this.recipient = recipient;
        this.count = count;
    }

    public String getRecipient() { return this.recipient; }
    public int getCount() {
        return this.count;
    }
}
