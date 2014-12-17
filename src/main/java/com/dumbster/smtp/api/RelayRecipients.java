package com.dumbster.smtp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class RelayRecipients {

    @XmlElement(name="recipient", type = String.class)
    private List<String> recipients;

    public RelayRecipients() {} // Empty constructor for JAXB

    public RelayRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public List<String> getRecipients() {
        return this.recipients;
    }
}
