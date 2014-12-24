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

import com.dumbster.smtp.api.MailMessage;
import com.dumbster.smtp.utils.EmailSender;
import com.dumbster.smtp.utils.RandomData;
import com.dumbster.smtp.utils.TestInfrastructure;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.*;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

@Test(groups = "Component")
public class SmtpServerTest {
    public static final String SENDER = "sender@here.com";
    private TestInfrastructure infrastructure = new TestInfrastructure();

    @BeforeClass
    private void startSmtpServer() throws Exception {
        infrastructure.startSmtpServer();
        infrastructure.startMailProcessor();
    }
    
    @AfterClass
    private void teardownInfrastructure() throws Exception {
        infrastructure.getSmtpServer().stop();
    }

    @Test
    public void sendNormalEmail() throws Exception {
        String recipient = RandomData.email();
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);

        emailSender.sendEmail(SENDER, recipient, "Test", "Test Body");
        verify(infrastructure.getMailMessageDao()).storeMessage(eq(recipient), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "Test");
        assertEquals(emailCaptor.getValue().getBody(), "Test Body");
    }

    @Test
    public void sendEmailWithCarriageReturn() throws Exception {
        String recipient = RandomData.email();
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);
        String bodyWithCR = "\n\nKeep these pesky carriage returns\n\n.\n\n...";

        emailSender.sendEmail(SENDER, recipient, "CR Test", bodyWithCR);
        verify(infrastructure.getMailMessageDao()).storeMessage(eq(recipient), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "CR Test");
        assertEquals(emailCaptor.getValue().getBody(), bodyWithCR);
    }

    @Test
    public void sendCharsetWithJapaneseMessage() throws Exception {
        String recipient = RandomData.email();
        EmailSender emailSender = newEmailSender();
        emailSender.setCharset("iso-2022-jp");
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);
        String body = "\u3042\u3044\u3046\u3048\u304a";

        emailSender.sendEmail(SENDER, recipient, "EncodedMessage", body);
        verify(infrastructure.getMailMessageDao()).storeMessage(eq(recipient), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "EncodedMessage");
        assertEquals(emailCaptor.getValue().getBody(), body);
    }


    @Test
    public void sendEncoding7BitJapaneseMessage() throws Exception {
        String recipient = RandomData.email();
        EmailSender emailSender = newEmailSender();
        String body = "\u3042\u3044\u3046\u3048\u304a";
        emailSender.setCharset("iso-2022-jp");
        emailSender.setEncoding("7bit");
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(SENDER, recipient, "EncodedMessage", body);
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());
        verify(infrastructure.getMailMessageDao()).storeMessage(eq(recipient), emailCaptor.capture());

        assertEquals(messageCaptor.getValue().getBody(), body);
    }

    @Test
    public void sendEncodingQuotedPrintableJapaneseMessage() throws Exception {
        String recipient = RandomData.email();
        EmailSender emailSender = newEmailSender();
        String body = "\u3042\u3044\u3046\u3048\u304a";
        emailSender.setCharset("iso-2022-jp");
        emailSender.setEncoding("quoted-printable");
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);

        emailSender.sendEmail(SENDER, recipient, "EncodedMessage", body);
        verify(infrastructure.getMailMessageDao()).storeMessage(eq(recipient), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "EncodedMessage");
        assertEquals(emailCaptor.getValue().getBody(), body);
    }

    @Test
    public void sendEncodingBase64EncodedJapaneseMessage() throws Exception {
        String recipient = RandomData.email();
        EmailSender emailSender = newEmailSender();
        String body = "\u3042\u3044\u3046\u3048\u304a";
        emailSender.setCharset("iso-2022-jp");
        emailSender.setEncoding("base64");
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);

        emailSender.sendEmail(SENDER, recipient, "EncodedMessage", body);
        verify(infrastructure.getMailMessageDao()).storeMessage(eq(recipient), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "EncodedMessage");
        assertEquals(emailCaptor.getValue().getBody(), body);
    }

    @Test
    public void continuedHeadersArriveIntact() throws Exception {
        EmailSender emailSender = newEmailSender();
        String recipient = RandomData.email();
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);
        emailSender.addHeaderLine("X-LongHeader: 12345");
        emailSender.addHeaderLine("\t67890");
        emailSender.addHeaderLine("X-LongerHeader: baz");
        emailSender.addHeaderLine("   foo bar");
        emailSender.addHeaderLine(" quux");

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(SENDER, recipient, "EncodedMessage", "Some text");
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());


        assertEquals(messageCaptor.getValue().getHeaderValue("X-LongHeader"), "1234567890");
        assertEquals(messageCaptor.getValue().getHeaderValue("X-LongerHeader"), "baz  foo barquux");
    }

    @Test
    public void sendTwoMessagesSameConnection() throws Exception {
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);
        Session smtpSession = emailSender.newSmtpSession(SENDER);
        String[] recipients = {RandomData.email(), RandomData.email()};
        MimeMessage first = createMessage(smtpSession, SENDER, recipients[0], "Doodle1", "Bug1");
        MimeMessage second = createMessage(smtpSession, SENDER, recipients[1], "Doodle2", "Bug2");

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(SENDER, first, second);
        verify(infrastructure.getSmtpMessageObserver(), times(2)).notify(messageCaptor.capture());

        assertEquals(messageCaptor.getAllValues().get(1).getBody(), "Bug2");
    }

    @Test
    public void sendTwoMessagesWithLogin() throws Exception {
        EmailSender emailSender = newEmailSender();
        emailSender.setUsername("username");
        emailSender.setPassword("password");
        Session smtpSession = emailSender.newSmtpSession(SENDER);
        MimeMessage first = createMessage(smtpSession, SENDER, RandomData.email(), "Doodle1", "Bug1");
        MimeMessage second = createMessage(smtpSession, SENDER, RandomData.email(), "Doodle2", "Bug2");
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(SENDER, first, second);
        verify(infrastructure.getSmtpMessageObserver(), times(2)).notify(messageCaptor.capture());

        assertEquals(messageCaptor.getAllValues().get(1).getBody(), "Bug2");
    }

    private MimeMessage createMessage(Session session, String from, String to, String subject, String body) throws Exception {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(body);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        return msg;
    }

    private EmailSender newEmailSender() {
        return new EmailSender(TestInfrastructure.SMTP_HOSTNAME, TestInfrastructure.SMTP_PORT);
    }
}
