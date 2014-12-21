package com.dumbster.smtp.storage;

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.transport.old.SimpleSmtpServer;
import com.dumbster.smtp.utils.EmailSender;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MailStorageTest {
    private static final String SMTP_HOSTNAME = "localhost";
    private final int SMTP_PORT = 12500;
    private static final String MAILS_FOLDER = System.getProperty("user.dir") + "/" + "emails";
    public static final String SQLITE_DB_FILENAME = "test.db";
    public static final String EMAIL_1 = "someone_first@somewhere.com";
    public static final String EMAIL_2 = "someone_second@somewhere.com";

    private SimpleSmtpServer simpleSmtpServer;
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


    private void startMockServer(IMailStorage mailStorage) {
        simpleSmtpServer = SimpleSmtpServer.start(SMTP_PORT);
        mailProcessor = new MailProcessor();
        simpleSmtpServer.addObserver(mailProcessor);
        mailProcessor.setMailStorage(mailStorage);
        Thread smtpMockServerThread = new Thread(mailProcessor);
        smtpMockServerThread.start();
    }

    @AfterMethod
    private void stopSmtpMockServer() {
        simpleSmtpServer.stop();
        mailProcessor.stop();
    }

}