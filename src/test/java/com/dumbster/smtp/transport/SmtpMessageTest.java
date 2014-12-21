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

import com.beust.jcommander.internal.Lists;
import com.dumbster.smtp.utils.EmailSender;
import com.dumbster.smtp.utils.SimpleSmtpStorage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;


@Test
public class SmtpMessageTest {

    public static final int PORT = 2500;
    public static final String RECIPIENT = "sender@email.com";
    private static final String MAIL_FROM = "test.from@teste.com";
    public static final String CHARSET = "UTF-8";
    private SmtpServer smtpServer;
    private SimpleSmtpStorage storage;
    private EmailSender emailSender;

    @BeforeClass
    private void setUp() throws Exception {
        this.smtpServer = SmtpServer.start(PORT);
        this.storage = new SimpleSmtpStorage();
        this.smtpServer.addObserver(this.storage);
        emailSender = new EmailSender("localhost", PORT);
    }

    @AfterClass
    public void tearDown() throws Exception {
        smtpServer.stop();
    }

    public void sendEmail() throws Exception {
        int countBefore = storage.getReceivedEmailSize();

        emailSender.sendEmail(MAIL_FROM, RECIPIENT, "Some subject", "Some message text");

        assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
    }

    public void sendEmailMultipleRecipients() throws Exception {
        int countBefore = storage.getReceivedEmailSize();
        String recipient = "sender@email.com, otherSender@test.com";

        emailSender.sendEmail(MAIL_FROM, recipient, "Test", "Test Body");

        assertEquals(storage.getReceivedEmailSize(), countBefore + 1);
    }

    public void sendEmailFrom() throws Exception {
        this.sendMessage(RECIPIENT, "subject", "Test Body");

        assertFrom("\"test.from@teste.com\" <test.from@teste.com>");
    }

    public void sendEmailSubject() throws Exception {
        String subject = "Test Ão çÇá";

        emailSender.sendEmail(MAIL_FROM, RECIPIENT, subject, "Some message text");

        assertSubject(subject);
    }

    public void sendEmailSubjectExtended() throws Exception {
        String subject = "Test Subject with very Long Text (over 76 chars) and special chars: "
                + "http://youtube.com/xyz äüö and secret informations";

        emailSender.sendEmail(MAIL_FROM, RECIPIENT, subject, "Test Body");

        assertSubject(subject);
    }

    public void sendEmailBody() throws Exception{
        String body = "Ão çÇá";

        emailSender.sendEmail(MAIL_FROM, RECIPIENT, "Subject", body);

        assertBody(body);
    }

    public void sendEmailBodyMultiline() throws Exception{
        String body = "Somthing\nNew Line\n\nTwo new Lines.\n\n...etc.\n\n\n.";

        this.sendMessage("sender@email.com", "Test", body);

        assertBody(body);
    }

    private void assertSubject(final String expected) {
        assertHeader("Subject", expected);
    }

    private void assertFrom(final String expected) {
        assertHeader("From", expected);
    }

    private void assertHeader(final String property, final String expected) {
        String headerValue = storage.getLatestEmail().getHeaderValue(property);
        assertEquals(headerValue, expected);
    }

    private void assertBody(final String expected) {
        assertEquals(storage.getLatestEmail().getBody(), expected);
    }

    private static void setHeaders(final MimeMessage message) throws Exception {
        message.setHeader("Content-Type", "text/plain; charset=UTF-8; format=flowed");
        message.setHeader("X-Accept-Language", "pt-br, pt");
        message.setHeader("Content-Transfer-Encoding", "quoted-printable");
    }

    private void sendMessage(final String recipientsCsv, final String subject, final String body) {
        final Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", PORT);
        properties.put("mail.mime.charset", CHARSET);
        properties.put("mail.from", MAIL_FROM);
        final Session session = Session.getInstance(properties, null);
        final MimeMessage msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(MAIL_FROM, MAIL_FROM));
            List<InternetAddress> addresses = Lists.newArrayList();
            String[] recipients = recipientsCsv.split(",");
            for (String email : recipients) {
                addresses.add(new InternetAddress(email, email));
            }
            msg.setRecipients(Message.RecipientType.TO, addresses.toArray(new InternetAddress[addresses.size()]));
            msg.setSubject(subject, CHARSET);
            msg.setText(body, CHARSET);
            setHeaders(msg);

            Transport.send(msg);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
