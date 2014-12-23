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
import com.dumbster.smtp.utils.TestInfrastructure;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

@Test(groups = "Component")
public class SmtpServerTest {

    public static final String RECIPIENT = "recipient@here.com";
    public static final String SENDER = "sender@here.com";
    private TestInfrastructure infrastructure;

    @Test
    public void restartServerMulitpleTimes() throws Exception {
        for (int i = 0; i < 5; i++) {
            stopSmtpServer();
            startSmtpServer();
        }
    }

    @Test
    public void sendNormalEmail() throws Exception {
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);

        emailSender.sendEmail(SENDER, RECIPIENT, "Test", "Test Body");
        verify(infrastructure.getMailMessageDao()).storeMessage(anyString(), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "Test");
        assertEquals(emailCaptor.getValue().getBody(), "Test Body");
    }

    @Test
    public void sendEmailWithCarriageReturn() throws Exception {
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);
        String bodyWithCR = "\n\nKeep these pesky carriage returns\n\n.\n\n...";

        emailSender.sendEmail(SENDER, RECIPIENT, "CR Test", bodyWithCR);
        verify(infrastructure.getMailMessageDao()).storeMessage(anyString(), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "CR Test");
        assertEquals(emailCaptor.getValue().getBody(), bodyWithCR);
    }

    @Test
    public void sendCharsetWithJapaneseMessage() throws Exception {
        EmailSender emailSender = newEmailSender();
        emailSender.setCharset("iso-2022-jp");
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);
        String body = "\u3042\u3044\u3046\u3048\u304a";

        emailSender.sendEmail(SENDER, RECIPIENT, "EncodedMessage", body);
        verify(infrastructure.getMailMessageDao()).storeMessage(anyString(), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "EncodedMessage");
        assertEquals(emailCaptor.getValue().getBody(), body);
    }


    @Test
    public void sendEncoding7BitJapaneseMessage() throws Exception {
        EmailSender emailSender = newEmailSender();
        String body = "\u3042\u3044\u3046\u3048\u304a";
        emailSender.setCharset("iso-2022-jp");
        emailSender.setEncoding("7bit");
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);
        
        emailSender.sendEmail(SENDER, RECIPIENT, "EncodedMessage", body);
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());
        verify(infrastructure.getMailMessageDao()).storeMessage(anyString(), emailCaptor.capture());

        assertEquals(messageCaptor.getValue().getBody(), body);
    }

    @Test
    public void sendEncodingQuotedPrintableJapaneseMessage() throws Exception {
        EmailSender emailSender = newEmailSender();
        String body = "\u3042\u3044\u3046\u3048\u304a";
        emailSender.setCharset("iso-2022-jp");
        emailSender.setEncoding("quoted-printable");
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);

        emailSender.sendEmail(SENDER, RECIPIENT, "EncodedMessage", body);
        verify(infrastructure.getMailMessageDao()).storeMessage(anyString(), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "EncodedMessage");
        assertEquals(emailCaptor.getValue().getBody(), body);
    }

    @Test
    public void sendEncodingBase64EncodedJapaneseMessage() throws Exception {
        EmailSender emailSender = newEmailSender();
        String body = "\u3042\u3044\u3046\u3048\u304a";
        emailSender.setCharset("iso-2022-jp");
        emailSender.setEncoding("base64");
        ArgumentCaptor<MailMessage> emailCaptor = ArgumentCaptor.forClass(MailMessage.class);

        emailSender.sendEmail(SENDER, RECIPIENT, "EncodedMessage", body);
        verify(infrastructure.getMailMessageDao()).storeMessage(anyString(), emailCaptor.capture());

        assertEquals(emailCaptor.getValue().getSubject(), "EncodedMessage");
        assertEquals(emailCaptor.getValue().getBody(), body);
    }

    @Test
    public void continuedHeadersArriveIntact() throws Exception {
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);
        emailSender.addHeaderLine("X-LongHeader: 12345");
        emailSender.addHeaderLine("\t67890");
        emailSender.addHeaderLine("X-LongerHeader: baz");
        emailSender.addHeaderLine("   foo bar");
        emailSender.addHeaderLine(" quux");

        emailSender.sendEmail(SENDER, RECIPIENT, "EncodedMessage", "Some text");
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());


        assertEquals(messageCaptor.getValue().getHeaderValue("X-LongHeader"), "1234567890");
        assertEquals(messageCaptor.getValue().getHeaderValue("X-LongerHeader"), "baz  foo barquux");
    }

    @Test
    public void sendTwoMessagesSameConnection() throws Exception {
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);
        Session smtpSession = emailSender.newSmtpSession(SENDER);
        MimeMessage first = createMessage(smtpSession, SENDER, "receiver@home.com", "Doodle1", "Bug1");
        MimeMessage second = createMessage(smtpSession, SENDER, "receiver@home.com", "Doodle2", "Bug2");

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
        MimeMessage first = createMessage(smtpSession, SENDER, "receiver@home.com", "Doodle1", "Bug1");
        MimeMessage second = createMessage(smtpSession, SENDER, "receiver@home.com", "Doodle2", "Bug2");
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        emailSender.sendEmail(SENDER, first, second);
        verify(infrastructure.getSmtpMessageObserver(), times(2)).notify(messageCaptor.capture());

        assertEquals(messageCaptor.getAllValues().get(1).getBody(), "Bug2");
    }


    @BeforeMethod
    private void startSmtpServer() throws Exception {
        infrastructure = new TestInfrastructure();
        infrastructure.inject();
        infrastructure.getSmtpServer().start();
    }

    @AfterMethod
    private void stopSmtpServer() throws Exception {
        infrastructure.getSmtpServer().stop();
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
