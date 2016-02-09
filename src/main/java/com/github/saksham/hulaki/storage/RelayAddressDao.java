package com.github.saksham.hulaki.storage;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RelayAddressDao {
    List<String> getRelayRecipients();

    void clearRelayRecipients();

    void addRelayRecipient(String email);

    boolean isRelayRecipient(String email);

    void removeRelayRecipient(String email);
}
