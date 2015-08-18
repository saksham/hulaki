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

package com.hulaki.smtp.api;

import com.beust.jcommander.internal.Lists;
import com.hulaki.smtp.storage.MailMessageDao;
import com.hulaki.smtp.utils.TestGroups;
import com.hulaki.smtp.utils.TestInfrastructure;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Test(groups = TestGroups.COMPONENT)
public class ApiClientTest {
    private static final String SUBJECT = "some subject";
    private static final String BODY = "some body";
    
    private IApiClient apiClient = new ApiClient("localhost", TestInfrastructure.API_PORT);
    private TestInfrastructure testInfrastructure;

    @Test
    public void countRequestSentToServer() throws Exception {
        MailMessageDao mailMessageDao = testInfrastructure.getMailMessageDao();
        when(mailMessageDao.countAllMessagesReceived()).thenReturn(0);

        int mailsCount = apiClient.countMails();

        assertEquals(mailsCount, 0);
    }

    @Test
    public void getRequestProcessedByServer() throws Exception {
        int expectedEmailsCount = 10;
        MailMessageDao mailMessageDao = testInfrastructure.getMailMessageDao();
        when(mailMessageDao.retrieveMessages(anyString())).thenReturn(newMailMessageList(expectedEmailsCount));

        List<MailMessage> messages = apiClient.getMessages("recipient");

        assertEquals(messages.size(), expectedEmailsCount);
    }

    @BeforeMethod
    private void startApiServer() throws Exception {
        testInfrastructure = new TestInfrastructure();
        testInfrastructure.startApiServer();
    }

    @AfterMethod
    private void stopApiServer() throws Exception {
        testInfrastructure.stop();
    }


    private List<MailMessage> newMailMessageList(int expectedEmailsCount) {
        List<MailMessage> messages = Lists.newArrayList();
        for (int i = 0; i < expectedEmailsCount; i++) {
            MailMessage mail = new MailMessage("from", "to", SUBJECT, BODY, false);
            messages.add(mail);
        }
        return messages;
    }

}
