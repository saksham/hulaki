package com.dumbster.smtp.app;

import com.dumbster.smtp.storage.InMemoryMailStorage;
import com.dumbster.smtp.storage.InMemoryRelayAddressStorage;
import com.dumbster.smtp.transport.ApiServer;
import com.dumbster.smtp.transport.old.SimpleSmtpServer;

public class MockServerApp {
    public static final int SMTP_SERVER_PORT = 2500;
    public static final int API_SERVER_PORT = 6869;
    private MailProcessor mailProcessor;
    private ApiServer apiServer;
    private SimpleSmtpServer smtpServer;

    public static void main(String[] args) throws Exception {
        InMemoryMailStorage mailStorage = new InMemoryMailStorage();
        InMemoryRelayAddressStorage relayAddressStorage = new InMemoryRelayAddressStorage();

        MockServerApp app = new MockServerApp();
        app.mailProcessor = new MailProcessor();
        app.mailProcessor.setMailStorage(mailStorage);
        app.mailProcessor.setRelayAddressStorage(relayAddressStorage);

        Thread mailProcessorThread = new Thread(app.mailProcessor);
        mailProcessorThread.start();

        app.smtpServer = SimpleSmtpServer.start(SMTP_SERVER_PORT);
        app.smtpServer.addObserver(app.mailProcessor);

        app.apiServer = new ApiServer();
        app.apiServer.setSmtpServer(app.smtpServer);
        app.apiServer.setApiServerPort(API_SERVER_PORT);
        app.apiServer.setMailStorage(mailStorage);
        app.apiServer.setRelayAddressStorage(relayAddressStorage);
        Thread apiServerThread = new Thread(app.apiServer);
        apiServerThread.start();

        System.out.println("Press any key to quit: ");
        System.in.read();

        app.mailProcessor.stop();
        app.smtpServer.stop();
        app.apiServer.stop();
    }
}
