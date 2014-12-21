package com.dumbster.smtp.storage;

import java.util.List;

public interface RelayAddressDao {
    List<String> getRelayRecipients();

    void clearRelayRecipients();

    void addRelayRecipient(String email);

    boolean isRelayRecipient(String email);

    void removeRelayRecipient(String email);
}
