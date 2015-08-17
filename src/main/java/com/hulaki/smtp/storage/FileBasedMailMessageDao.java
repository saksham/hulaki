package com.hulaki.smtp.storage;


import com.hulaki.smtp.api.MailMessage;
import com.hulaki.smtp.exceptions.SmtpException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileBasedMailMessageDao implements MailMessageDao {
    private static final String EMAIL_FILE_PREFIX = "EML_";
    public static final String EMAIL_FILE_SUFFIX = ".xml";
    private static final Logger logger = LogManager.getLogger(FileBasedMailMessageDao.class);
    private final File mailsFolder;
    private Map<String, List<String>> fileNamesByRecipients = Maps.newConcurrentMap();
    private int storedEmailsCount = 0;

    public FileBasedMailMessageDao(String mailsFolderPath) {
        this.mailsFolder = recreateFolder(mailsFolderPath);
    }

    @Override
    public synchronized void storeMessage(String recipient, MailMessage email) {
        ensureFolderExists(this.mailsFolder.getPath());

        try {
            File recipientFolder = getRecipientMailsFolder(recipient);
            ensureFolderExists(recipientFolder.getPath());
            File emailFile = File.createTempFile(EMAIL_FILE_PREFIX, EMAIL_FILE_SUFFIX, recipientFolder);
            marshal(email, emailFile);

            if (!fileNamesByRecipients.containsKey(recipient)) {
                fileNamesByRecipients.put(recipient, new ArrayList<String>());
            }
            fileNamesByRecipients.get(recipient).add(emailFile.getName());
            this.storedEmailsCount++;
            logger.info("Stored message for " + recipient + ", sum total: " + storedEmailsCount);

        } catch (IOException ioe) {
            throw new SmtpException("Failed to create file to store mail for " + recipient, ioe);
        }
    }

    @Override
    public synchronized void clearMessagesForRecipient(String recipient) {
        if (!fileNamesByRecipients.containsKey(recipient)) {
            return;
        }

        ensureFolderExists(this.mailsFolder.getPath());
        this.storedEmailsCount -= fileNamesByRecipients.get(recipient).size();
        fileNamesByRecipients.remove(recipient);
        deleteRecipientFolder(recipient);
        logger.info("Deleted all messages for " + recipient + ", sum total: " + storedEmailsCount);
    }

    @Override
    public synchronized List<MailMessage> retrieveMessages(String recipient) {
        List<MailMessage> mails = Lists.newArrayList();
        if (!fileNamesByRecipients.containsKey(recipient)) {
            return mails;
        }

        ensureFolderExists(this.mailsFolder.getPath());
        File recipientFolder = getRecipientMailsFolder(recipient);
        for (String file : fileNamesByRecipients.get(recipient)) {
            MailMessage mailMessage = unmarshal(new File(recipientFolder.getPath(), file));
            mails.add(mailMessage);
        }

        logger.info("Retrieved " + mails.size() + " messages for " + recipient + ", sum total: " + storedEmailsCount);
        return mails;
    }

    @Override
    public synchronized void clearMessages() {
        try {
            FileUtils.deleteDirectory(this.mailsFolder);
            this.storedEmailsCount = 0;
            this.fileNamesByRecipients.clear();
            logger.info("Deleted ALL messages sum total: " + storedEmailsCount);
        } catch(IOException ex) {
            throw new SmtpException(ex);
        }
    }

    @Override
    public synchronized int countMessagesForRecipient(String recipient) {
        if (!fileNamesByRecipients.containsKey(recipient)) {
            return 0;
        }
        return fileNamesByRecipients.get(recipient).size();
    }

    @Override
    public synchronized int countAllMessagesReceived() {
        return this.storedEmailsCount;
    }

    private File recreateFolder(String folderPath) {
        try {
            FileUtils.forceDelete(new File(folderPath));
        } catch (FileNotFoundException ex) {
            //Ignore because we don't need to delete a non-existent file.
        } catch (IOException ioe) {
            throw new SmtpException("Could not clear the mails folder.", ioe);
        }
        return ensureFolderExists(folderPath);
    }

    private File ensureFolderExists(String folderPath) {
        try {
            File folder = new File(folderPath);
            FileUtils.forceMkdir(folder);
            return folder;
        } catch(IOException ex) {
            throw new SmtpException(ex);
        }
    }

    private File getRecipientMailsFolder(String recipient) {
        return new File(this.mailsFolder.getPath(), recipient);
    }

    private void deleteRecipientFolder(String recipient) {
        try {
            File recipientFolder = getRecipientMailsFolder(recipient);
            FileUtils.deleteDirectory(recipientFolder);
        } catch(IOException ex) {
            throw new SmtpException(ex);
        }
    }

    private static MailMessage unmarshal(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(MailMessage.class);
            Unmarshaller um = context.createUnmarshaller();
            return (MailMessage) um.unmarshal(new FileReader(file));
        } catch (Exception ex) {
            throw new SmtpException("Could not serialize the mail message.", ex);
        }
    }

    public static void marshal(MailMessage mail, File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(MailMessage.class);
            Marshaller m = context.createMarshaller();
            m.marshal(mail, file);
        } catch (Exception ex) {
            throw new SmtpException("Could not deserialize the saved mail message.", ex);
        }
    }
}
