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
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

@Test(groups = "Performance")
public class SmtpServerPerformanceTest {
    private TestInfrastructure testInfrastructure = new TestInfrastructure();
    private EmailSender emailSender = new EmailSender(TestInfrastructure.SMTP_HOSTNAME, TestInfrastructure.SMTP_PORT);

    @BeforeClass
    public void startInfrastructure() throws Exception {
        testInfrastructure.startSmtpServer();
        testInfrastructure.startApiServer();
        testInfrastructure.startMailProcessor();
    }

    @AfterClass
    public void teardownInfrastructure() throws Exception {
        testInfrastructure.stop();
    }

    @Test(threadPoolSize = 50, invocationCount = 50)
    public void handles50ConcurrentSmtpConnections() {
        String sender = RandomStringUtils.randomAlphanumeric(20) + "@email.com";
        String recipient = RandomStringUtils.randomAlphanumeric(20) + "@email.com";
        ArgumentCaptor<MailMessage> messageCaptor = ArgumentCaptor.forClass(MailMessage.class);

        emailSender.sendEmail(sender, recipient, "Subject", "Body " + RandomStringUtils.random(10));
        verify(testInfrastructure.getMailMessageDao()).storeMessage(eq(recipient), messageCaptor.capture());

        assertEquals(messageCaptor.getValue().getFrom(), sender);
        assertEquals(messageCaptor.getValue().getTo(), recipient);
    }


    @Test
    public void restartSmtpServerMulitpleTimes() throws Exception {
        for (int i = 0; i < 5; i++) {
            testInfrastructure.getSmtpServer().start();
            testInfrastructure.getSmtpServer().stop();
        }
    }
}
