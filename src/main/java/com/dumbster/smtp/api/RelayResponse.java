package com.dumbster.smtp.api;


import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "relay-response", namespace = "https://github.com/saksham/dumbster")
public class RelayResponse extends ApiResponse {

    @XmlElement()
    private RelayRecipients relayRecipients;


    public RelayResponse() {
    } // Empty constructor for JAXB

    public RelayResponse(List<String> recipients) {
        this.relayRecipients = new RelayRecipients(recipients);
    }

    public RelayRecipients getRelayRecipients() {
        return relayRecipients;
    }
}
