package com.github.saksham.hulaki.transport;

import com.github.saksham.hulaki.storage.MailMessageDao;
import com.github.saksham.hulaki.app.MailProcessor;
import com.github.saksham.hulaki.storage.RelayAddressDao;
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
