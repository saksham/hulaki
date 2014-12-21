package com.dumbster.smtp.storage;


import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

public class InMemoryRelayAddressDao implements RelayAddressDao {
    private Set<String> relayRecipients = Sets.newConcurrentHashSet();

    @Override
    public synchronized ArrayList<String> getRelayRecipients() {
        return new ArrayList<>(this.relayRecipients);
    }

    @Override
    public synchronized void clearRelayRecipients() {
        this.relayRecipients.clear();
    }

    @Override
    public synchronized void addRelayRecipient(String email) {
        this.relayRecipients.add(email);
    }

    @Override
    public synchronized boolean isRelayRecipient(String email) {
        return this.relayRecipients.contains(email);
    }

    @Override
    public synchronized void removeRelayRecipient(String email) {
        this.relayRecipients.remove(email);
    }


}