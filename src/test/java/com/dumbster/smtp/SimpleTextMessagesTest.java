/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import junit.framework.TestCase;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Simple TestCase with Text Messages, contains special Chars in Header and Body and Multiline Tests.
 * <p/>
 * Original TestCase from <a href="http://blogs.abril.com.br/java-cabeca/2009/05/dumpster-testando-envio-email.html">
 * Dumbster - Testando envio de Email </a>.
 *
 * @author Leonardo Campos
 * @author Harald Brabenetz
 */
public class SimpleTextMessagesTest extends TestCase {

    private static final String MAIL_HOST = "localhost";

    private static final String CHARSET = "UTF-8";

    private static final String MAIL_FROM = "test.from@teste.com";

    private SimpleSmtpServer mockServer;

    private InMemoryMailStorage storage;

    public void setUp() {
        this.mockServer = SimpleSmtpServer.start();
        this.storage = new InMemoryMailStorage();
        this.mockServer.addObserver(this.storage);
    }

    public void tearDown() {
        if (this.mockServer != null) {
            this.mockServer.stop();
        }
    }

    private void sendMessage(final String emails, final String subject, final String body) {
        final Properties properties = new Properties();
        properties.put("mail.host", MAIL_HOST);
        properties.put("mail.mime.charset", CHARSET);
        properties.put("mail.from", MAIL_FROM);
        final Session session = Session.getInstance(properties, null);
        final MimeMessage msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(MAIL_FROM, MAIL_FROM));
            List addresses = new ArrayList();
            String[] recipients = emails.split(",");
            for (String email : recipients) {
                addresses.add(new InternetAddress(email, email));
            }
            msg.setRecipients(Message.RecipientType.TO, (InternetAddress[]) addresses
                    .toArray(new InternetAddress[addresses.size()]));
            msg.setSubject(subject, CHARSET);
            msg.setText(body, CHARSET);
            setHeaders(msg);

            Transport.send(msg);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setHeaders(final MimeMessage message) throws MessagingException {
        message.setHeader("Content-Type", "text/plain; charset=UTF-8; format=flowed");
        message.setHeader("X-Accept-Language", "pt-br, pt");
        message.setHeader("Content-Transfer-Encoding", "quoted-printable");
    }

    public void testEnviarEmailSent() {
        this.sendMessage("sender@email.com", "Test", "Test Body");
        assertEquals(1, storage.getReceivedEmailSize());
    }

    public void testEnviarEmailSentMulti() {
        this.sendMessage("sender@email.com, otherSender@test.com", "Test", "Test Body");
        assertEquals(1, storage.getReceivedEmailSize());
    }

    public void testEnviarEmailFrom() {
        this.sendMessage("sender@email.com", "Test", "Test Body");
        assertFrom("\"test.from@teste.com\" <test.from@teste.com>");
    }

    public void testEnviarEmailSubject() {
        this.sendMessage("sender@email.com", "Test Ão çÇá", "Test Body");
        assertSubject("Test Ão çÇá");
    }

    public void testEnviarEmailSubjectExtended() {
        String subject = "Test Subject with very Long Text (over 76 chars) and special chars: "
                + "http://youtube.com/xyz äüö and secret informations";
        this.sendMessage("sender@email.com", subject, "Test Body");
        assertSubject(subject);
    }

    public void testEnviarEmailBody() {
        this.sendMessage("sender@email.com", "Test", "Ão çÇá");
        assertBody("Ão çÇá");
    }

    public void testEnviarEmailBodyMultiline() {
        String testBody = "Somthing\nNew Line\n\nTwo new Lines.\n\n...etc.\n\n\n.";
        this.sendMessage("sender@email.com", "Test", testBody);
        assertBody(testBody);
    }

    private void assertSubject(final String expected) {
        assertHeader("Subject", expected);
    }

    private void assertFrom(final String expected) {
        assertHeader("From", expected);
    }

    private void assertHeader(final String property, final String expected) {
        String headerValue = getEmailSent().getHeaderValue(property);
        assertEquals(expected, headerValue);
    }

    private void assertBody(final String expected) {
        assertEquals(expected, getEmailSent().getBody());
    }

    private SmtpMessage getEmailSent() {
        final SmtpMessage email = storage.getLatestMail();
        System.out.println("BODY:\n" + email.getBody());
        return email;
    }
}
