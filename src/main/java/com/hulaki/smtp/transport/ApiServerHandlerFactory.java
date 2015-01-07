package com.hulaki.smtp.transport;

import com.hulaki.smtp.app.MailProcessor;
import com.hulaki.smtp.storage.MailMessageDao;
import com.hulaki.smtp.storage.RelayAddressDao;
import org.springframework.stereotype.Component;

@Component
public class ApiServerHandlerFactory {
    private final MailMessageDao mailMessageDao;
    private final RelayAddressDao relayAddressDao;
    private final MailProcessor mailProcessor;
    private final SmtpServer smtpServer;

    public ApiServerHandlerFactory(MailMessageDao mailMessageDao, RelayAddressDao relayAddressDao, MailProcessor mailProcessor, SmtpServer smtpServer) {
        this.mailMessageDao = mailMessageDao;
        this.relayAddressDao = relayAddressDao;
        this.mailProcessor = mailProcessor;
        this.smtpServer = smtpServer;
    }


    public ApiServerHandler create() {
        return new ApiServerHandler(mailMessageDao, relayAddressDao, mailProcessor, smtpServer);
    }
}
