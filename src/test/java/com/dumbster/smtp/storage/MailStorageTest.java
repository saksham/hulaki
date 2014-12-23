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

package com.dumbster.smtp.storage;

import com.dumbster.smtp.utils.TestInfrastructure;
import com.dumbster.smtp.utils.EmailSender;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(groups = "Component")
public class MailStorageTest {
    public static final String SQLITE_DB_FILENAME = "target/test.db";
    public static final String EMAIL_1 = "someone_first@somewhere.com";
    public static final String EMAIL_2 = "someone_second@somewhere.com";
    private static final String MAILS_FOLDER = System.getProperty("user.dir") + "/" + "target/emails";
    private EmailSender emailSender = new EmailSender(TestInfrastructure.SMTP_HOSTNAME, TestInfrastructure.SMTP_PORT);
    private TestInfrastructure apiInfrastructure;

    @Test(dataProvider = "provideMailStorages")
    public void shouldStoreAndRetrieveEmails(MailMessageDao mailStorage) throws Exception {
        // Given
        startServer(mailStorage);
        String subject = "Subject " + RandomStringUtils.randomAlphabetic(15);
        String messageBody = "Body - " + RandomStringUtils.randomAlphabetic(100);

        // When
        for (int i = 0; i < 3; i++) {
            emailSender.sendEmail(EMAIL_1, EMAIL_1, subject + i, messageBody + i);
            emailSender.sendEmail(EMAIL_1, EMAIL_2, subject + i, messageBody + i);
        }

        // Then
        assertEquals(mailStorage.countAllMessagesReceived(), 3 * 2, "All emails should be received");
        assertEquals(mailStorage.countMessagesForRecipient(EMAIL_1), 3, "Email should have been stored by the server");
        assertEquals(mailStorage.retrieveMessages(EMAIL_1).get(0).getSubject(), subject + "0", "Body should match");
        assertEquals(mailStorage.retrieveMessages(EMAIL_1).get(0).getBody(), messageBody + "0", "Body should match");


        // When
        mailStorage.clearMessages();

        // Then
        assertEquals(mailStorage.countAllMessagesReceived(), 0);
    }


    @DataProvider
    private Object[][] provideMailStorages() {
        return new Object[][]{
                {new InMemoryMailMessageDao()},
                {new FileBasedMailMessageDao(MAILS_FOLDER)},
                {new SqliteMailMessageDao(SQLITE_DB_FILENAME)}
        };
    }



    private void startServer(MailMessageDao mailStorage) throws Exception {
        apiInfrastructure = new TestInfrastructure();
        apiInfrastructure.setMailMessageDao(mailStorage);
        apiInfrastructure.inject();
        apiInfrastructure.startSmtpServer();
        apiInfrastructure.startMailProcessor();
    }

    @AfterMethod
    private void stopServer() throws Exception {
        apiInfrastructure.stop();
    }

}