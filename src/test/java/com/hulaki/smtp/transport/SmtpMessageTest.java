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
package com.hulaki.smtp.transport;

import com.hulaki.smtp.utils.EmailSender;
import com.hulaki.smtp.utils.RandomData;
import com.hulaki.smtp.utils.TestInfrastructure;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.*;

import javax.mail.internet.InternetAddress;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;


@Test(groups = "Component")
public class SmtpMessageTest {
    private static final String MAIL_FROM = "sender@email.com";

    private TestInfrastructure infrastructure = new TestInfrastructure();

    @BeforeClass
    private void setUp() throws Exception {
        infrastructure.startSmtpServer();
    }

    @AfterClass
    public void teardownInfrastructure() throws Exception {
        infrastructure.stop();
    }

    @Test
    public void sendEmail() throws Exception {
        EmailSender emailSender = newEmailSender();
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        emailSender.sendEmail(MAIL_FROM, RandomData.email(), "Some subject", "Some message text");
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());

        assertEquals(messageCaptor.getValue().getBody(), "Some message text");
    }

    @Test
    public void sendEmailFrom() throws Exception {
        EmailSender emailSender = newEmailSender();
        String from = new InternetAddress(MAIL_FROM, MAIL_FROM).toString();
        String recipient = RandomData.email();
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(from, recipient, "Subject", "Body - " + RandomStringUtils.random(10));
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());

        assertFromHeaderEquals(messageCaptor.getValue(), from);
        assertEquals(messageCaptor.getValue().getHeaderValue("To"), new InternetAddress(recipient, recipient).toString());
    }

    @Test
    public void sendEmailSubject() throws Exception {
        EmailSender emailSender = newEmailSender();
        String subject = "Test Ão çÇá";
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(MAIL_FROM, RandomData.email(), subject, "Test Body");
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());

        assertSubjectEquals(messageCaptor.getValue(), subject);
    }

    @Test
    public void sendEmailSubjectExtended() throws Exception {
        EmailSender emailSender = newEmailSender();
        String subject = "Test Subject with very Long Text (over 76 chars) and special chars: "
                + "http://youtube.com/xyz äüö and secret informations";
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(MAIL_FROM, RandomData.email(), subject, "Body - " + RandomStringUtils.random(10));
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());

        assertSubjectEquals(messageCaptor.getValue(), subject);
    }

    @Test
    public void sendEmailBody() throws Exception {
        EmailSender emailSender = newEmailSender();
        String body = "Ão çÇá";
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(MAIL_FROM, RandomData.email(), "Subject - " + RandomStringUtils.random(10), body);
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());

        assertBodyEquals(messageCaptor.getValue(), body);
    }

    @Test
    public void sendEmailBodyMultiline() throws Exception {
        EmailSender emailSender = newEmailSender();
        String body = "Somthing\nNew Line\n\nTwo new Lines.\n\n...etc.\n\n\n.";
        ArgumentCaptor<SmtpMessage> messageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(MAIL_FROM, RandomData.email(), "Subject - " + RandomStringUtils.random(10), body);
        verify(infrastructure.getSmtpMessageObserver()).notify(messageCaptor.capture());

        assertBodyEquals(messageCaptor.getValue(), body);
    }

    private void assertSubjectEquals(final SmtpMessage message, final String expected) {
        assertHeaderEquals(message, "Subject", expected);
    }

    private void assertFromHeaderEquals(final SmtpMessage message, final String expected) {
        assertHeaderEquals(message, "From", expected);
    }

    private void assertHeaderEquals(final SmtpMessage message, final String property, final String expected) {
        String headerValue = message.getHeaderValue(property);
        assertEquals(headerValue, expected);
    }

    private void assertBodyEquals(final SmtpMessage message, final String expected) {
        assertEquals(message.getBody(), expected);
    }

    private EmailSender newEmailSender() throws Exception {
        EmailSender emailSender = new EmailSender(TestInfrastructure.SMTP_HOSTNAME, TestInfrastructure.SMTP_PORT);
        emailSender.addHeader("Content-Type", "text/plain; charset=UTF-8; format=flowed");
        emailSender.addHeader("X-Accept-Language", "pt-br, pt");
        emailSender.addHeader("Content-Transfer-Encoding", "quoted-printable");
        return emailSender;
    }
}
