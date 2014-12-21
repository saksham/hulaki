package com.dumbster.smtp.app;

import com.dumbster.smtp.entities.MailMessage;
import com.dumbster.smtp.storage.IMailStorage;
import com.dumbster.smtp.storage.IRelayAddressStorage;
import com.dumbster.smtp.transport.Observer;
import com.dumbster.smtp.transport.SmtpMessage;
import com.dumbster.smtp.utils.EmailSender;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailProcessor implements Observer<SmtpMessage>, Runnable {
    private static final Logger logger = Logger.getLogger(MailProcessor.class);

    private IMailStorage mailStorage;

    public void setRelayAddressStorage(IRelayAddressStorage relayAddressStorage) {
        this.relayAddressStorage = relayAddressStorage;
    }

    private IRelayAddressStorage relayAddressStorage;
    private EmailSender emailSender;
    private volatile boolean stopped;

    public void setMailStorage(IMailStorage mailStorage) {
        this.mailStorage = mailStorage;
    }

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }


    @Override
    public void notify(SmtpMessage smtpMessage) {
        String recipient = normalizeEmailAddress(smtpMessage.getHeaderValue("To"));
        boolean wasMailRelayed = false;

        if (relayAddressStorage.isRelayRecipient(recipient)) {
            wasMailRelayed = true;
            emailSender.sendEmail(smtpMessage.getHeaderValue("From"), recipient,
                    smtpMessage.getHeaderValue("Subject"), smtpMessage.getBody());
            logger.info("Relayed email to " + recipient);
        }

        MailMessage message = new MailMessage(smtpMessage, wasMailRelayed);
        mailStorage.storeMessage(recipient, message);
    }

    private static String normalizeEmailAddress(String email) {
        Pattern pattern = Pattern.compile("<[^<]*@[^>]*>");
        Matcher matcher = pattern.matcher(email);

        if (matcher.find()) {
            return email.substring(matcher.start() + 1, matcher.end() - 1);
        } else {
            return email;
        }
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void run() {
        this.stopped = false;
        while (!this.stopped) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                this.stopped = true;
            }
        }
    }
}
