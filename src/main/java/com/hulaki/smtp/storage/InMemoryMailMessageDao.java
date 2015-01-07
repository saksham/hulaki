package com.hulaki.smtp.storage;


import com.hulaki.smtp.api.MailMessage;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;

public class InMemoryMailMessageDao implements MailMessageDao {

    private Map<String, ArrayList<MailMessage>> emailsByRecipients = Maps.newConcurrentMap();
    private int storedEmailCount = 0;
    private Logger logger = Logger.getLogger(this.getClass());

    public synchronized void storeMessage(String recipient, MailMessage email) {
        if (!emailsByRecipients.containsKey(recipient)) {
            emailsByRecipients.put(recipient, new ArrayList<MailMessage>());
        }
        ArrayList<MailMessage> mailsForUser = emailsByRecipients.get(recipient);
        mailsForUser.add(email);
        storedEmailCount++;
        logger.info("Stored message # " + mailsForUser.size() + " for " + recipient + " (total: " + storedEmailCount +")...");
    }

    public synchronized ArrayList<MailMessage> retrieveMessages(String recipient) {
        ArrayList<MailMessage> result = new ArrayList<>();
        if (emailsByRecipients.containsKey(recipient)) {
            result.addAll(emailsByRecipients.get(recipient));
        }
        logger.info("Retrieved " + result.size() + " messages for " + recipient);
        return result;
    }

    public synchronized int countMessagesForRecipient(String recipient) {
        int count = 0;
        if (this.emailsByRecipients.containsKey(recipient)) {
            count = this.emailsByRecipients.get(recipient).size();
        }
        logger.info("Counted  " + count + " messages for " + recipient);
        return count;
    }

    public synchronized void clearMessagesForRecipient(String recipient) {
        if (this.emailsByRecipients.containsKey(recipient)) {
            int count = this.emailsByRecipients.get(recipient).size();
            this.storedEmailCount -= count;
            this.emailsByRecipients.get(recipient).clear();
            this.emailsByRecipients.remove(recipient);
            logger.info("Cleared  " + count + " messages for " + recipient);
        }
    }

    public synchronized void clearMessages() {
        this.emailsByRecipients.clear();
        logger.info("Cleared  " + this.storedEmailCount + " messages");
        this.storedEmailCount = 0;
    }


    public synchronized int countAllMessagesReceived() {
        logger.info("Counted " + this.storedEmailCount + " messages");
        return this.storedEmailCount;
    }

}
