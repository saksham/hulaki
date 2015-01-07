package com.hulaki.smtp.app;

import com.hulaki.smtp.api.MailMessage;
import com.hulaki.smtp.storage.MailMessageDao;
import com.hulaki.smtp.storage.RelayAddressDao;
import com.hulaki.smtp.transport.Observer;
import com.hulaki.smtp.transport.SmtpMessage;
import com.hulaki.smtp.utils.EmailSender;
import com.hulaki.smtp.utils.EmailUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

@Component
public class MailProcessor implements Observer<SmtpMessage>, Runnable {

    private static final Logger logger = Logger.getLogger(MailProcessor.class);

    private volatile boolean stopped;

    private MailMessageDao mailMessageDao;
    private RelayAddressDao relayAddressDao;
    private EmailSender emailSender;

    @Override
    public void notify(SmtpMessage smtpMessage) {
        String[] toAddresses = smtpMessage.getHeaderValue("To").split(",");
        for (String toAddress : toAddresses) {
            String recipient = EmailUtils.normalizeEmailAddress(toAddress);
            String sender = smtpMessage.getHeaderValue("From");
            String subject = smtpMessage.getHeaderValue("Subject");
            String body = smtpMessage.getBody();
            process(sender, recipient, subject, body);
        }
    }

    private void process(String sender, String recipient, String subject, String body) {
        boolean wasMailRelayed = false;
        if (relayAddressDao.isRelayRecipient(recipient)) {
            wasMailRelayed = true;
            emailSender.sendEmail(sender, recipient, subject, body);
            logger.info("Relayed email to " + recipient);
        }

        MailMessage message = new MailMessage(sender, recipient, subject, body, wasMailRelayed);
        mailMessageDao.storeMessage(recipient, message);
    }

    public void stop() {
        logger.info("Stopping Mail processor...");
        this.stopped = true;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void run() {
        logger.info("Starting Mail processor...");
        this.stopped = false;
        logger.info("Mail processor started!");
        while (!this.stopped) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                this.stopped = true;
            }
        }
        logger.info("Mail processor stopped!");
    }

    @Required
    public void setMailMessageDao(MailMessageDao mailMessageDao) {
        this.mailMessageDao = mailMessageDao;
    }

    @Required
    public void setRelayAddressDao(RelayAddressDao relayAddressDao) {
        this.relayAddressDao = relayAddressDao;
    }

    @Required
    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }
}
