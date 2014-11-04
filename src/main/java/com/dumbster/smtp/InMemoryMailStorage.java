package com.dumbster.smtp;

import java.util.ArrayList;
import java.util.List;

public class InMemoryMailStorage implements Observer<SmtpMessage>, MailStorage {

    /**
     * Stores all of the email received since this instance started up.
     */
    private List<SmtpMessage> smtpMessages;

    public InMemoryMailStorage() {
        this.smtpMessages = new ArrayList<>();
    }

    @Override
    public void notify(SmtpMessage data) {
        smtpMessages.add(data);
    }

    /**
     * Get the number of messages received.
     * @return size of received email list
     */
    public synchronized int getReceivedEmailSize() {
        return smtpMessages.size();
    }


    /**
     * Clears the collection that stores received email messages.
     */
    public synchronized void clearReceivedEmails() {
        smtpMessages.clear();
    }

    public SmtpMessage getLatestMail() {
        if(smtpMessages.size() == 0) {
            return null;
        }
        return smtpMessages.get(smtpMessages.size() - 1);
    }
}
