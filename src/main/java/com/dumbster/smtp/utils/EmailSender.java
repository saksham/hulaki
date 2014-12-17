package com.dumbster.smtp.utils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {
    private String smtpHostname;
    private int smtpPort;


    public EmailSender(String smtpHostname, int smtpPort) {
        this.smtpHostname = smtpHostname;
        this.smtpPort = smtpPort;
    }

    public void setSmtpHostname(String smtpHostname) {
        this.smtpHostname = smtpHostname;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }


    public void sendEmail(String from, String to, String subject, String messageBody) {
        sendEmail(from, to, subject, messageBody, smtpHostname, smtpPort);
    }

    public static void sendEmail(String from, String to, String subject, String messageBody, String smtpHostname, int smtpPort) {

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", smtpHostname);
        properties.setProperty("mail.smtp.port", Integer.toString(smtpPort));

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try{
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(messageBody);


            Transport.send(message);
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
