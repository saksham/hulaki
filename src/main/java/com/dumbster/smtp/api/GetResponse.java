package com.dumbster.smtp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "get-response", namespace = "https://github.com/saksham/dumbster")
public class GetResponse extends ApiResponse {

    private String recipient;

    @XmlElement(name = "mail-messages", type = MailMessages.class)
    private MailMessages messages;

    public GetResponse() { } // Empty constructor for JAXB

    public GetResponse(String recipient, List<MailMessage> smtpMessages) {
        this.recipient = recipient;
        this.messages = new MailMessages(smtpMessages);
    }

    public String getRecipient() {
        return this.recipient;
    }

    public MailMessages getMessages() {
        return this.messages;
    }
}
