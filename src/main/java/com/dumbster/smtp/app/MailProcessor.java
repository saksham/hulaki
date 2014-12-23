package com.dumbster.smtp.app;

import com.dumbster.smtp.api.MailMessage;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import com.dumbster.smtp.transport.Observer;
import com.dumbster.smtp.transport.SmtpMessage;
import com.dumbster.smtp.utils.EmailSender;
import com.dumbster.smtp.utils.EmailUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class MailProcessor implements Observer<SmtpMessage>, Runnable {

    private static final Logger logger = Logger.getLogger(MailProcessor.class);

    private volatile boolean stopped;

    private MailMessageDao mailMessageDao;
    private RelayAddressDao relayAddressDao;
    private EmailSender emailSender;

    @Override
    public void notify(SmtpMessage smtpMessage) {
        String recipient = EmailUtils.normalizeEmailAddress(smtpMessage.getHeaderValue("To"));
        boolean wasMailRelayed = false;

        if (relayAddressDao.isRelayRecipient(recipient)) {
            wasMailRelayed = true;
            emailSender.sendEmail(smtpMessage.getHeaderValue("From"), recipient,
                    smtpMessage.getHeaderValue("Subject"), smtpMessage.getBody());
            logger.info("Relayed email to " + recipient);
        }

        MailMessage message = new MailMessage(smtpMessage, wasMailRelayed);
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
