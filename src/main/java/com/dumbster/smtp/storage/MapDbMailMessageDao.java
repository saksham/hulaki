package com.dumbster.smtp.storage;

import com.dumbster.smtp.api.MailMessage;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

public class MapDbMailMessageDao implements MailMessageDao {
    private static final String COLLECTION_NAME = "emails";
    private static final Logger logger = Logger.getLogger(MapDbMailMessageDao.class);
    
    private volatile int storedEmailsCount = 0;
    private final ConcurrentNavigableMap<String, List<MailMessage>> map;
    private final DB db;
    private final String dbFilepath;


    public MapDbMailMessageDao(String dbFilepath) {
        this.dbFilepath = dbFilepath;
        this.db = DBMaker.newFileDB(new File(this.dbFilepath))
                .closeOnJvmShutdown()
                .compressionEnable()
                .make();
        this.map = db.getTreeMap(COLLECTION_NAME);
        this.map.clear();
    }

    @Override
    public synchronized void storeMessage(String recipient, MailMessage email) {
        storedEmailsCount++;
        List<MailMessage> emails = Lists.newArrayList();
        if (map.containsKey(recipient)) {
            emails.addAll(map.get(recipient));
        }
        emails.add(email);
        map.put(recipient, emails);
        db.commit();
        logger.info("Stored message #" + emails.size() + " for " + recipient + ". Sum-total: " + storedEmailsCount);
    }

    @Override
    public synchronized List<MailMessage> retrieveMessages(String recipient) {
        List<MailMessage> emails = Lists.newArrayList();
        if (map.containsKey(recipient)) {
            emails = Lists.newArrayList(map.get(recipient));
        }
        logger.info("Retrieved " + emails.size() + " emails for " + recipient);
        return emails;
    }

    @Override
    public synchronized int countMessagesForRecipient(String recipient) {
        int count = 0;
        if (map.containsKey(recipient)) {
            count = map.get(recipient).size();
        }
        logger.info("Counted " + count + " emails for " + recipient);
        return count;
    }

    @Override
    public synchronized void clearMessagesForRecipient(String recipient) {
        int countForRecipient = 0;
        if(map.containsKey(recipient)) {
            countForRecipient = map.get(recipient).size();
            storedEmailsCount -= countForRecipient;
            map.remove(recipient);
            db.commit();
        } 
        logger.info("Cleared " + countForRecipient + " emails for " + recipient + ". Sum-total: " + storedEmailsCount);
    }

    @Override
    public synchronized void clearMessages() {
        int previousCount = storedEmailsCount;
        storedEmailsCount = 0;
        map.clear();
        db.commit();
        logger.info("Cleared " + previousCount + " emails. Sum-total: " + storedEmailsCount);
    }

    @Override
    public synchronized int countAllMessagesReceived() {
        logger.info("Counted total of " + storedEmailsCount + " emails.");
        return storedEmailsCount;
    }
}
