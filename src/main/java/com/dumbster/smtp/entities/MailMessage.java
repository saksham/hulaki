package com.dumbster.smtp.entities;

import com.dumbster.smtp.transport.SmtpMessage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mail-message", namespace = "https://github.com/saksham/dumbster")
public class MailMessage implements Serializable {
    private static final long serialVersionUID = -7673328451852229617L;

    private String to;
    private String from;
    private String subject;
    private String body;
    private boolean isRelayed;

    public MailMessage() {
    } // Empty constructor for JAXB

    public MailMessage(SmtpMessage message, boolean isRelayed) {
        this.to = message.getHeaderValue("To");
        this.from = message.getHeaderValue("From");
        this.subject = message.getHeaderValue("Subject");
        this.body = message.getBody();
        this.isRelayed = isRelayed;
    }

    public MailMessage(String from, String to, String subject, String body, boolean isRelayed) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.isRelayed = isRelayed;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isRelayed() {
        return isRelayed;
    }

    @Override
    public String toString() {
        return "To: " + to + ", Subject: " + subject;
    }
}
