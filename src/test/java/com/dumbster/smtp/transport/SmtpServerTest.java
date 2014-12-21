/**
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
package com.dumbster.smtp.transport;

import com.dumbster.smtp.utils.EmailSender;
import com.dumbster.smtp.utils.SimpleSmtpStorage;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Test
public class SmtpServerTest {

    /**
     * General Logger for this Class.
     */
    private static final Logger LOG = Logger.getLogger(SmtpServerTest.class);
    public static final int PORT = 2500;
    public static final String RECIPIENT = "recipient@here.com";
    public static final String SENDER = "sender@here.com";
    private SimpleSmtpStorage storage;
    private SmtpServer smtpServer;

    @BeforeClass
    private void setUp() throws Exception {
        storage = new SimpleSmtpStorage();
        smtpServer = SmtpServer.start(PORT);
        smtpServer.addObserver(storage);
    }

    @AfterClass
    private void tearDown() throws Exception {
        smtpServer.removeObserver(storage);
        smtpServer.stop();
    }

    public void sendEmail() throws Exception {
        int countBefore = storage.getReceivedEmailSize();

        EmailSender.sendEmail(SENDER, RECIPIENT, "Test", "Test Body", "localhost", PORT);

        assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
        SmtpMessage email = getEmailSent();
        assertEquals(email.getHeaderValue("Subject"), "Test");
        assertEquals(email.getBody(), "Test Body");
    }

    public void sendMessageWithCarriageReturn() {
        //int countBefore = storage.getReceivedEmailSize();
        String bodyWithCR = "\n\nKeep these pesky carriage returns\n\n.\n\n...";
        try {
            sendMessage(PORT, "sender@hereagain.com", "CRTest", bodyWithCR, "receivingagain@there.com");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            fail("Unexpected exception: " + e);
        }

        //assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
        SmtpMessage email = getEmailSent();
        assertEquals(email.getBody(), bodyWithCR);
    }

    public void sendTwoMessagesSameConnection() {

        //int countBefore = storage.getReceivedEmailSize();

        try {
            MimeMessage[] mimeMessages = new MimeMessage[2];
            Properties mailProps = getMailProperties(PORT);
            Session session = Session.getInstance(mailProps, null);
            //session.setDebug(true);

            mimeMessages[0] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle1", "Bug1");
            mimeMessages[1] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle2", "Bug2");

            Transport transport = session.getTransport("smtp");
            transport.connect("localhost", PORT, null, null);

            for (MimeMessage mimeMessage : mimeMessages) {
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            }

            transport.close();
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
            fail("Unexpected exception: " + e);
        }

        //assertEquals(storage.getReceivedEmailSize(), countBefore + 2);
    }

    public void sendTwoMsgsWithLogin() {


        int countBefore = storage.getReceivedEmailSize();
        try {
            String Server = "localhost";
            String From = "sender@here.com";
            String To = "receiver@there.com";
            String Subject = "Test";
            String body = "Test Body";

            Properties props = System.getProperties();

            props.put("mail.smtp.host", Server);

            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(From));

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
                transport.connect(Server, PORT, "ddd", "ddd");
                transport.sendMessage(msg, InternetAddress.parse(To, false));
                transport.sendMessage(msg, InternetAddress.parse("dimiter.bakardjiev@musala.com", false));

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

        assertEquals(storage.getReceivedEmailSize(), countBefore + 2);
        SmtpMessage email = getEmailSent();
        assertEquals(email.getHeaderValue("Subject"), "Test");
        assertEquals(email.getBody(), "Test Body");
    }

    /**
     * TestCase from
     * http://sourceforge.net/tracker/?func=detail&aid=1354585&group_id=78413&atid=553188
     *
     * @throws MessagingException if an error occurs
     */
    public void continuedHeadersArriveIntact() throws MessagingException {
        int countBefore = storage.getReceivedEmailSize();

        Session session = Session.getInstance(getMailProperties(PORT), null);
        MimeMessage msg = createMessage(session, "sender@example.com", "guy@example.net", "Re: Hello", "Virus");
        msg.addHeaderLine("X-LongHeader: 12345");
        msg.addHeaderLine("\t67890");
        msg.addHeaderLine("X-LongerHeader: baz");
        msg.addHeaderLine("   foo bar");
        msg.addHeaderLine(" quux");

        Transport.send(msg);

        assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
        SmtpMessage recvd = storage.getLatestEmail();
        assertEquals(recvd.getHeaderValue("X-LongHeader"), "1234567890");
        assertEquals(recvd.getHeaderValue("X-LongerHeader"), "baz  foo barquux");
    }

    /**
     * TestCase from
     * http://sourceforge.net/tracker/?func=detail&aid=1313597&group_id=78413&atid=553188
     *
     * @throws MessagingException if an error occurs
     */
    public void sendCharsetMessage() throws MessagingException {
        //int countBefore = storage.getReceivedEmailSize();

        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        String charset = "iso-2022-jp";
        sendMessage(PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", charset);

        //assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
        SmtpMessage email = getEmailSent();
        // TODO: uncomment the following assertion
        //assertEquals(email.getCharset(), charset);
        assertEquals(email.getBody(), body);
    }

    /**
     * TestCase Content-Transfer-Encoding "7bit".
     *
     * @throws MessagingException if an error occurs
     */
    public void sendEncoding7BitMessage() throws MessagingException {
        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        sendMessage(PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", "iso-2022-jp", "7bit");

        assertEquals(getEmailSent().getBody(), body);
    }

    /**
     * TestCase Content-Transfer-Encoding "quoted-printable" (default).
     *
     * @throws MessagingException if an error occurs
     */
    public void sendEncodingQuotedPrintableMessage() throws MessagingException {
        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        sendMessage(PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", "iso-2022-jp", "quoted-printable");

        assertEquals(getEmailSent().getBody(), body);
    }

    /**
     * TestCase Content-Transfer-Encoding "base64" (default).
     *
     * @throws MessagingException if an error occurs
     */
    public void sendEncodingBase64Message() throws MessagingException {
        // some Japanese letters
        String body = "\u3042\u3044\u3046\u3048\u304a";
        sendMessage(PORT, "sender@hereagain.com", "EncodedMessage", body,
                "receivingagain@there.com", "iso-2022-jp", "base64");

        assertEquals(getEmailSent().getBody(), body);
    }

    private SmtpMessage getEmailSent() {
        return storage.getLatestEmail();
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
     * @param session  the javax Mail Session
     * @param from     The From Address must be a valid email.
     * @param to       The To Address must be a valid email.
     * @param subject  The Subject
     * @param body     The Body
     * @param charset  The Charset
     * @param encoding The Content-Transfer-Encoding. valid Values are "7bit",
     *                 "quoted-printable", "base64"
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
