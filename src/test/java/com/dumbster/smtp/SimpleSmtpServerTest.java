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

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.Properties;
import java.util.Date;

public class SimpleSmtpServerTest extends TestCase {

    /**
     * General Logger for this Class.
     */
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
            .getLog(SimpleSmtpServerTest.class);
    private static final int SMTP_PORT = 1081;

    private SimpleSmtpServer server;
    private InMemoryMailStorage storage;

    public SimpleSmtpServerTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
        server = SimpleSmtpServer.start(SMTP_PORT);
        storage = new InMemoryMailStorage();
        storage.clearReceivedEmails();
        server.addObserver(storage);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        server.stop();
        server.removeObserver(storage);
    }

    public void testSend() {
        try {
            sendMessage(SMTP_PORT, "sender@here.com", "Test", "Test Body", "receiver@there.com");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            fail("Unexpected exception: " + e);
        }

        assertTrue(storage.getReceivedEmailSize() == 1);
        SmtpMessage email = getEmailSent();
        assertTrue(email.getHeaderValue("Subject").equals("Test"));
        assertTrue(email.getBody().equals("Test Body"));
    }

    public void testSendMessageWithCarriageReturn() {
        String bodyWithCR = "\n\nKeep these pesky carriage returns\n\n.\n\n...";
        try {
            sendMessage(SMTP_PORT, "sender@hereagain.com", "CRTest", bodyWithCR, "receivingagain@there.com");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            fail("Unexpected exception: " + e);
        }

        assertEquals(1, storage.getReceivedEmailSize());
        SmtpMessage email = getEmailSent();
        assertEquals(bodyWithCR, email.getBody());
    }

    public void testSendTwoMessagesSameConnection() {
        try {
            MimeMessage[] mimeMessages = new MimeMessage[2];
            Properties mailProps = getMailProperties(SMTP_PORT);
            Session session = Session.getInstance(mailProps, null);
            //session.setDebug(true);

            mimeMessages[0] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle1", "Bug1");
            mimeMessages[1] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle2", "Bug2");

            Transport transport = session.getTransport("smtp");
            transport.connect("localhost", SMTP_PORT, null, null);

            for (MimeMessage mimeMessage : mimeMessages) {
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            }

            transport.close();
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
            fail("Unexpected exception: " + e);
        }

        assertTrue(storage.getReceivedEmailSize() == 2);
    }

    public void testSendTwoMsgsWithLogin() {
        try {
            String Server = "localhost";
            String From = "sender@here.com";
            String To = "receiver@there.com";
            String Subject = "Test";
            String body = "Test Body";

            Properties props = System.getProperties();

            if (Server != null) {
                props.put("mail.smtp.host", Server);
            }

            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);

            if (From != null) {
                msg.setFrom(new InternetAddress(From));
            } else {
                msg.setFrom();
            }

            InternetAddress.parse(To, false);
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(To, false));
            msg.setSubject(Subject);

            msg.setText(body);
            msg.setHeader("X-Mailer", "musala");
            msg.setSentDate(new Date());
            msg.saveChanges();

            Transport transport = null;

            try {
                transport = session.getTransport("smtp");
                transport.connect(Server, SMTP_PORT, "ddd", "ddd");
                transport.sendMessage(msg, InternetAddress.parse(To, false));
                transport.sendMessage(msg, InternetAddress.parse("dimiter.bakardjiev@musala.com", false));

            } catch (javax.mail.MessagingException e) {
                LOG.error(e.getMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (transport != null) {
                    transport.close();
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        assertTrue(storage.getReceivedEmailSize() == 2);
        SmtpMessage email = getEmailSent();
        assertTrue(email.getHeaderValue("Subject").equals("Test"));
        assertTrue(email.getBody().equals("Test Body"));
    }

    /**
     * TestCase from
     * http://sourceforge.net/tracker/?func=detail&aid=1476278&group_id=78413&atid=553188
     */
    public void testStartReturnsNullOnBindError() {
        SimpleSmtpServer duplicate = SimpleSmtpServer.start(SMTP_PORT);
        assertNull(duplicate);
    }

    /**
     * TestCase from
     * http://sourceforge.net/tracker/?func=detail&aid=1354585&group_id=78413&atid=553188
     *
     * @throws MessagingException if an error occurs
     */
    public void testContinuedHeadersArriveIntact() throws MessagingException {
        Session session = Session.getInstance(getMailProperties(SMTP_PORT), null);
        MimeMessage msg = createMessage(session, "sender@example.com", "guy@example.net", "Re: Hello", "Virus");
        msg.addHeaderLine("X-LongHeader: 12345");
        msg.addHeaderLine("\t67890");
        msg.addHeaderLine("X-LongerHeader: baz");
        msg.addHeaderLine("   foo bar");
        msg.addHeaderLine(" quux");

        Transport.send(msg);

        assertEquals(1, storage.getReceivedEmailSize());
        SmtpMessage recvd = (SmtpMessage) storage.getLatestMail();
        assertEquals("1234567890", recvd.getHeaderValue("X-LongHeader"));
        assertEquals("baz  foo barquux", recvd.getHeaderValue("X-LongerHeader"));
    }

    /**
     * TestCase from
     * http://sourceforge.net/tracker/?func=detail&aid=1313597&group_id=78413&atid=553188
     *
     * @throws MessagingException if an error occurs
     */
    public void testSendCharsetMessage() throws MessagingException {
        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        String charset = "iso-2022-jp";
        sendMessage(SMTP_PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", charset);

        assertTrue(storage.getReceivedEmailSize() == 1);
        SmtpMessage email = getEmailSent();
        assertEquals(charset, email.getCharset());
        assertEquals(body, email.getBody());
    }

    /**
     * TestCase Content-Transfer-Encoding "7bit".
     *
     * @throws MessagingException if an error occurs
     */
    public void testSendEncoding7BitMessage() throws MessagingException {
        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        sendMessage(SMTP_PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", "iso-2022-jp", "7bit");

        assertEquals(body, getEmailSent().getBody());
    }

    /**
     * TestCase Content-Transfer-Encoding "quoted-printable" (default).
     *
     * @throws MessagingException if an error occurs
     */
    public void testSendEncodingQuotedPrintableMessage() throws MessagingException {
        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        sendMessage(SMTP_PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", "iso-2022-jp", "quoted-printable");

        assertEquals(body, getEmailSent().getBody());
    }

    /**
     * TestCase Content-Transfer-Encoding "base64" (default).
     *
     * @throws MessagingException if an error occurs
     */
    public void testSendEncodingBase64Message() throws MessagingException {
        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        sendMessage(SMTP_PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", "iso-2022-jp", "base64");

        assertEquals(body, getEmailSent().getBody());
    }

    private SmtpMessage getEmailSent() {
        return storage.getLatestMail();
    }

    private Properties getMailProperties(int port) {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.smtp.host", "localhost");
        mailProps.setProperty("mail.smtp.port", "" + port);
        mailProps.setProperty("mail.smtp.sendpartial", "true");
        return mailProps;
    }

    private void sendMessage(int port, String from, String subject, String body, String to) throws MessagingException {
        sendMessage(port, from, subject, body, to, null);
    }

    private void sendMessage(int port, String from, String subject, String body, String to, String charset) throws MessagingException {
        sendMessage(port, from, subject, body, to, charset, null);
    }

    private void sendMessage(int port, String from, String subject, String body, String to, String charset, String encoding) throws MessagingException {

        Properties mailProps = getMailProperties(port);
        Session session = Session.getInstance(mailProps, null);
        //session.setDebug(true);

        MimeMessage msg = createMessage(session, from, to, subject, body, charset, encoding);
        Transport.send(msg);
    }

    private MimeMessage createMessage(
            Session session, String from, String to, String subject, String body) throws MessagingException {
        return createMessage(session, from, to, subject, body, null, null);
    }

    /**
     * @param session the javax Mail Session
     * @param from The From Address must be a valid email.
     * @param to The To Address must be a valid email.
     * @param subject The Subject
     * @param body The Body
     * @param charset The Charset
     * @param encoding The Content-Transfer-Encoding. valid Values are "7bit",
     * "quoted-printable", "base64"
     * @return the new created MimeMessage.
     * @throws MessagingException
     */
    private MimeMessage createMessage(//
            final Session session, //
            final String from, //
            final String to, //
            final String subject, //
            final String body, //
            final String charset, //
            final String encoding) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        if (charset != null) {
            msg.setText(body, charset);
        } else {
            msg.setText(body);
        }
        if (encoding != null) {
            // see http://en.wikipedia.org/wiki/MIME#Content-Transfer-Encoding
            msg.setHeader("Content-Transfer-Encoding", encoding);
        }
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        return msg;
    }
}
