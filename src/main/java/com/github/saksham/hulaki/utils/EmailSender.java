package com.github.saksham.hulaki.utils;

import com.github.saksham.hulaki.exceptions.SmtpException;
import com.google.common.collect.Lists;

import javax.mail.Message;
import javax.mail.MessagingException;
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
    private List<String> additionalHeaderLines = Lists.newArrayList();

    private final String smtpHostname;
    private final int smtpPort;
    private String username;
    private String password;


    public EmailSender(String smtpHostname, int smtpPort) {
        this.smtpHostname = smtpHostname;
        this.smtpPort = smtpPort;
    }

    public void sendEmail(String from, String to, String subject, String body) {
        sendEmail(from, new String[]{to}, subject, body);
    }

    public void sendEmail(String from, MimeMessage... mimeMessages) throws MessagingException {
        Session session = newSmtpSession(from);
        Transport transport = session.getTransport("smtp");
        transport.connect(smtpHostname, smtpPort, username, password);
        
        for(MimeMessage message : mimeMessages) {
            if(encoding != null) {
                message.setHeader("Content-Transfer-Encoding", encoding);
            }
            message.setFrom(new InternetAddress(from));
            for(String headerLine : additionalHeaderLines) {
                message.addHeaderLine(headerLine);
            }
            transport.sendMessage(message, message.getAllRecipients());
        }
        transport.close();
    }
    
    public void sendEmail(String from, String[] to, String subject, String body) {
        Session session = newSmtpSession(from);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/plain; charset=" + charset + "; format=flowed");
            message.setHeader("X-Accept-Language", "pt-br, pt");
            List<InternetAddress> recipients = Lists.newArrayList();
            for (String email : to) {
                recipients.add(new InternetAddress(email, email));
            }
            message.setFrom(new InternetAddress(from));
            message.addRecipients(Message.RecipientType.TO, recipients.toArray(new InternetAddress[recipients.size()]));
            message.setSubject(subject);
            message.setText(body);
            
            sendEmail(from, message);
        } catch (Exception ex) {
            throw new SmtpException(ex);
        }
    }

    public Session newSmtpSession(String from) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", smtpHostname);
        properties.setProperty("mail.smtp.port", Integer.toString(smtpPort));
        properties.put("mail.mime.charset", charset);
        properties.put("mail.from", from);

        return Session.getDefaultInstance(properties);
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public void addHeaderLine(String headerLine) {
        additionalHeaderLines.add(headerLine);
    }
    
    public void addHeader(String headerKey, String headerValue) {
        addHeaderLine(headerKey + ": " + headerValue);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
