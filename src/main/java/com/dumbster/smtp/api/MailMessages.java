package com.dumbster.smtp.api;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class MailMessages {

    @XmlElement(name="mail-message", type = MailMessage.class)
    private List<MailMessage> messages;

    public MailMessages() {} // Empty constructor for JAXB

    public MailMessages(List<MailMessage> smtpMessages) {
        this.messages = new ArrayList<>(smtpMessages);
    }

    public List<MailMessage> getMessages() { return this.messages; }
}
