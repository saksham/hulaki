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

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.transport.SmtpServer;
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
    private static final String SMTP_HOSTNAME = "localhost";
    private static final String MAILS_FOLDER = System.getProperty("user.dir") + "/" + "target/emails";
    private final int SMTP_PORT = 12500;
    private SmtpServer smtpServer;
    private MailProcessor mailProcessor;
    private EmailSender emailSender = new EmailSender(SMTP_HOSTNAME, SMTP_PORT);

    @DataProvider
    private Object[][] provideMailStorages() {
        return new Object[][]{
                {new InMemoryMailStorage()},
                {new FileBasedMailStorage(MAILS_FOLDER)},
                {new SqliteMailStorage(SQLITE_DB_FILENAME)}
        };
    }


    @Test(dataProvider = "provideMailStorages")
    public void shouldStoreAndRetrieveEmails(IMailStorage mailStorage) throws Exception {
        // Given
        startMockServer(mailStorage);
        String subject = "Subject " + RandomStringUtils.randomAlphabetic(15);
        String messageBody = "Body - " + RandomStringUtils.randomAlphabetic(100);

        // When
        for (int i = 0; i < 3; i++) {
            emailSender.sendEmail(EMAIL_1, EMAIL_1, subject + i, messageBody + i);
            emailSender.sendEmail(EMAIL_1, EMAIL_2, subject + i, messageBody + i);
        }

        Thread.sleep(2000);

        // Then
        assertEquals(mailStorage.countAllMessagesReceived(), 3 * 2, "All emails should be received");
        assertEquals(mailStorage.countMessagesForRecipient(EMAIL_1), 3, "Email should have been stored by the server");
        assertEquals(mailStorage.retrieveMessages(EMAIL_1).get(0).getSubject(), subject + "0", "Body should match");
        assertEquals(mailStorage.retrieveMessages(EMAIL_1).get(0).getBody(), messageBody + "0", "Body should match");
    }



    private void startMockServer(IMailStorage mailStorage) throws Exception {
        smtpServer = SmtpServer.start(SMTP_PORT);
        mailProcessor = new MailProcessor();
        smtpServer.addObserver(mailProcessor);
        mailProcessor.setMailStorage(mailStorage);
        mailProcessor.setRelayAddressStorage(new InMemoryRelayAddressStorage());
        Thread smtpMockServerThread = new Thread(mailProcessor);
        smtpMockServerThread.start();
    }

    @AfterMethod
    private void stopSmtpMockServer() throws Exception {
        smtpServer.removeObserver(mailProcessor);
        smtpServer.stop();
        mailProcessor.stop();
    }

}