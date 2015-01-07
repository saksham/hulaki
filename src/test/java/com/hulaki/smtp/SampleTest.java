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

package com.hulaki.smtp;

import com.hulaki.smtp.api.ApiClient;
import com.hulaki.smtp.api.MailMessage;
import com.hulaki.smtp.storage.InMemoryMailMessageDao;
import com.hulaki.smtp.transport.SmtpMessage;
import com.hulaki.smtp.utils.EmailSender;
import com.hulaki.smtp.utils.RandomData;
import com.hulaki.smtp.utils.TestInfrastructure;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

@Test(groups = "Integration")
public class SampleTest {
    private TestInfrastructure infrastructure;
    private EmailSender emailSender = new EmailSender(TestInfrastructure.SMTP_HOSTNAME, TestInfrastructure.SMTP_PORT);
    private ApiClient apiClient = new ApiClient(TestInfrastructure.API_HOSTNAME, TestInfrastructure.API_PORT);

    @BeforeClass
    private void startAllServers() throws Exception {
        infrastructure = new TestInfrastructure(new InMemoryMailMessageDao());
        infrastructure.startMailProcessor();
        infrastructure.startSmtpServer();
        infrastructure.startApiServer();
    }
    
    @AfterClass
    private void tearDownInfrastructure() throws Exception {
        infrastructure.stop();
    }

    @Test
    public void startInfrastructureAndSendMessage() throws Exception {
        String sender = RandomData.email();
        String recipient = RandomData.email();
        String subject = RandomStringUtils.random(10);
        String body = RandomStringUtils.random(10);
        ArgumentCaptor<SmtpMessage> smtpMessageCaptor = ArgumentCaptor.forClass(SmtpMessage.class);

        infrastructure.resetSmtpMessageObserverMock();
        emailSender.sendEmail(sender, recipient, subject, body);
        verify(infrastructure.getSmtpMessageObserver()).notify(smtpMessageCaptor.capture());
        List<MailMessage> storedMessages = apiClient.getMessages(recipient);

        assertEquals(smtpMessageCaptor.getValue().getBody(), body);
        assertEquals(storedMessages.size(), 1);
    }
}
