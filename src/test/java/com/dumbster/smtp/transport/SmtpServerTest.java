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

@Test
public class SmtpServerTest {

    public static final int PORT = 2500;
    public static final String RECIPIENT = "recipient@here.com";
    public static final String SENDER = "sender@here.com";
    private SimpleSmtpStorage storage;
    private SmtpServer smtpServer;
    private EmailSender emailSender;

    @BeforeClass
    private void setUp() throws Exception {
        emailSender = new EmailSender("localhost", PORT);
        startSmtpServer();
    }

    @AfterClass
    private void tearDown() throws Exception {
        stopSmtpServer();
    }

    public void restartServerMulitpleTimes() throws Exception {
        for(int i = 0; i < 10; i ++) {
            stopSmtpServer();
            startSmtpServer();
        }
    }


    public void sendEmail() throws Exception {
        int countBefore = storage.getReceivedEmailSize();

        emailSender.sendEmail(SENDER, RECIPIENT, "Test", "Test Body");

        assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
        SmtpMessage email = storage.getLatestEmail();
        assertEquals(email.getHeaderValue("Subject"), "Test");
        assertEquals(email.getBody(), "Test Body");
    }

    public void sendMessageWithCarriageReturn() {
        int countBefore = storage.getReceivedEmailSize();
        String bodyWithCR = "\n\nKeep these pesky carriage returns\n\n.\n\n...";

        emailSender.sendEmail(SENDER, RECIPIENT, "CRTest", bodyWithCR);


        assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
        SmtpMessage email = storage.getLatestEmail();
        assertEquals(email.getBody(), bodyWithCR);
    }

    public void sendTwoMessagesSameConnection() throws Exception {

        int countBefore = storage.getReceivedEmailSize();

        MimeMessage[] mimeMessages = new MimeMessage[2];
        Properties mailProps = getMailProperties(PORT);
        Session session = Session.getInstance(mailProps, null);
        session.setDebug(true);

        mimeMessages[0] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle1", "Bug1");
        mimeMessages[1] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle2", "Bug2");

        Transport transport = session.getTransport("smtp");
        transport.connect("localhost", PORT, null, null);

        for (MimeMessage mimeMessage : mimeMessages) {
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        }
        transport.close();

        assertEquals(storage.getReceivedEmailSize(), countBefore + 2);
    }

    public void sendTwoMessagesWithLogin() throws Exception {


        int countBefore = storage.getReceivedEmailSize();

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

        Transport transport = session.getTransport("smtp");
        transport.connect(Server, PORT, "ddd", "ddd");
        transport.sendMessage(msg, InternetAddress.parse(To, false));
        transport.sendMessage(msg, InternetAddress.parse("joe.bloggs@test.com", false));


        assertEquals(storage.getReceivedEmailSize(), countBefore + 2);
        SmtpMessage email = storage.getLatestEmail();
        assertEquals(email.getHeaderValue("Subject"), "Test");
        assertEquals(email.getBody(), "Test Body");
    }

    public void continuedHeadersArriveIntact() throws MessagingException {
        int countBefore = storage.getReceivedEmailSize();

        Session session = Session.getInstance(getMailProperties(PORT), null);
        MimeMessage msg = createMessage(session, SENDER, RECIPIENT, "Re: Hello", "Virus");
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

    public void sendCharsetWithJapaneseMessage() throws MessagingException {
        int countBefore = storage.getReceivedEmailSize();
        String body = "\u3042\u3044\u3046\u3048\u304a";
        String charset = "iso-2022-jp";

        sendMessage(PORT, SENDER, "EncodedMessage", body, RECIPIENT, charset);

        assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
        SmtpMessage email = storage.getLatestEmail();
        assertEquals(email.getCharset(), charset);
        assertEquals(email.getBody(), body);
    }

    public void sendEncoding7BitJapaneseMessage() throws MessagingException {
        String body = "\u3042\u3044\u3046\u3048\u304a";

        sendMessage(PORT, SENDER, "EncodedMessage", body, RECIPIENT, "iso-2022-jp", "7bit");

        assertEquals(storage.getLatestEmail().getBody(), body);
    }


    public void sendEncodingQuotedPrintableJapaneseMessage() throws MessagingException {
        String body = "\u3042\u3044\u3046\u3048\u304a";

        sendMessage(PORT, SENDER, "EncodedMessage", body, RECIPIENT, "iso-2022-jp", "quoted-printable");

        assertEquals(storage.getLatestEmail().getBody(), body);
    }

    public void sendEncodingBase64EncodedJapaneseMessage() throws MessagingException {
        String body = "\u3042\u3044\u3046\u3048\u304a";

        sendMessage(PORT, SENDER, "EncodedMessage", body, RECIPIENT, "iso-2022-jp", "base64");

        assertEquals(storage.getLatestEmail().getBody(), body);
    }

    private void startSmtpServer() throws Exception {
        storage = new SimpleSmtpStorage();
        smtpServer = SmtpServer.start(PORT);
        smtpServer.addObserver(storage);
    }

    private void stopSmtpServer() throws Exception {
        smtpServer.removeObserver(storage);
        smtpServer.stop();
    }

    private Properties getMailProperties(int port) {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.smtp.host", "localhost");
        mailProps.setProperty("mail.smtp.port", "" + port);
        mailProps.setProperty("mail.smtp.sendpartial", "true");
        return mailProps;
    }

    private void sendMessage(int port, String from, String subject, String body, String to, String charset) throws MessagingException {
        sendMessage(port, from, subject, body, to, charset, null);
    }

    private void sendMessage(int port, String from, String subject, String body, String to, String charset, String encoding) throws MessagingException {
        Properties mailProps = getMailProperties(port);
        Session session = Session.getInstance(mailProps, null);

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
