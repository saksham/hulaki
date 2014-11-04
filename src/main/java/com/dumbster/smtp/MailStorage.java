package com.dumbster.smtp;

public interface MailStorage {

    /**
     * Get the number of messages received.
     * @return size of received email list
     */
    int getReceivedEmailSize();


    /**
     * Clears the collection that stores received email messages.
     */
    void clearReceivedEmails();

    /**
     * Retrieves the latest email
     * @return latest email
     */
    SmtpMessage getLatestMail();
}
