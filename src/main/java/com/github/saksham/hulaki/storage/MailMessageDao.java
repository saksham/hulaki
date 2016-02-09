package com.github.saksham.hulaki.storage;

import com.github.saksham.hulaki.api.MailMessage;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public interface MailMessageDao {
    void storeMessage(String recipient, MailMessage email);

    List<MailMessage> retrieveMessages(String recipient);

    int countMessagesForRecipient(String recipient);

    void clearMessagesForRecipient(String recipient);

    void clearMessages();

    int countAllMessagesReceived();
}
