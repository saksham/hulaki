package com.dumbster.smtp.utils;

import com.dumbster.smtp.exceptions.SmtpException;
import com.google.common.collect.Lists;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

public class EmailSender {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private String charset = DEFAULT_CHARSET;
    private String encoding;

    private final String smtpHostname;
    private final int smtpPort;


    public EmailSender(String smtpHostname, int smtpPort) {
        this.smtpHostname = smtpHostname;
        this.smtpPort = smtpPort;
    }

    public void sendEmail(String from, String to, String subject, String body) {
        sendEmail(from, new String[]{to}, subject, body);
    }

    public void sendEmail(String from, String[] to, String subject, String body) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", smtpHostname);
        properties.setProperty("mail.smtp.port", Integer.toString(smtpPort));
        properties.put("mail.mime.charset", charset);
        properties.put("mail.from", from);

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/plain; charset=" + charset + "; format=flowed");
            message.setHeader("X-Accept-Language", "pt-br, pt");
            if(encoding != null) {
                message.setHeader("Content-Transfer-Encoding", encoding);
            }
            message.setFrom(new InternetAddress(from));
            List<InternetAddress> recipients = Lists.newArrayList();
            for (String email : to) {
                recipients.add(new InternetAddress(email, email));
            }
            message.addRecipients(Message.RecipientType.TO, recipients.toArray(new InternetAddress[recipients.size()]));
            message.setSubject(subject);
            message.setText(body);


            Transport.send(message);
        } catch (Exception ex) {
            throw new SmtpException(ex);
        }
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
