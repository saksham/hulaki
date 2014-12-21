package com.dumbster.smtp.storage;
import com.dumbster.smtp.api.MailMessage;

import java.util.List;

public interface MailMessageDao {
    void storeMessage(String recipient, MailMessage email);

    List<MailMessage> retrieveMessages(String recipient);

    int countMessagesForRecipient(String recipient);

    void clearMessagesForRecipient(String recipient);

    void clearMessages();

    int countAllMessagesReceived();
}
