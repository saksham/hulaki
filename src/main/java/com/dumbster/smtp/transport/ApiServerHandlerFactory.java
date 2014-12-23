package com.dumbster.smtp.transport;

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import org.springframework.beans.factory.annotation.Required;

public class ApiServerHandlerFactory {
    private static volatile ApiServerHandlerFactory instance = null;
    private MailMessageDao mailMessageDao;
    private RelayAddressDao relayAddressDao;
    private MailProcessor mailProcessor;
    private SmtpServer smtpServer;

    private ApiServerHandlerFactory() {
    }


    public static ApiServerHandlerFactory getInstance() {
        if (instance == null) {
            synchronized (ApiServerHandlerFactory.class) {
                if (instance == null) {
                    instance = new ApiServerHandlerFactory();
                }
            }
        }
        return instance;
    }


    public ApiServerHandler create() {
        return new ApiServerHandler(mailMessageDao, relayAddressDao, mailProcessor, smtpServer);
    }

    @Required
    public void setMailMessageDao(MailMessageDao mailMessageDao) {
        this.mailMessageDao = mailMessageDao;
    }

    @Required
    public void setRelayAddressDao(RelayAddressDao relayAddressDao) {
        this.relayAddressDao = relayAddressDao;
    }

    @Required
    public void setMailProcessor(MailProcessor mailProcessor) {
        this.mailProcessor = mailProcessor;
    }

    @Required
    public void setSmtpServer(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }
}
