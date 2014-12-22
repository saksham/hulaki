package com.dumbster.smtp.app;

import com.dumbster.smtp.storage.InMemoryMailMessageDao;
import com.dumbster.smtp.storage.InMemoryRelayAddressDao;
import com.dumbster.smtp.transport.ApiServer;
import com.dumbster.smtp.transport.SmtpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MockServerApp {
    public static final int SMTP_SERVER_PORT = 2500;
    public static final int API_SERVER_PORT = 6869;
    private MailProcessor mailProcessor;
    private ApiServer apiServer;
    private SmtpServer smtpServer;

    public static void main(String[] args) throws Exception {
        InMemoryMailMessageDao mailMessageDao = new InMemoryMailMessageDao();
        InMemoryRelayAddressDao relayAddressDao = new InMemoryRelayAddressDao();

        MockServerApp app = new MockServerApp();
        app.mailProcessor = new MailProcessor();
        app.mailProcessor.setMailMessageDao(mailMessageDao);
        app.mailProcessor.setRelayAddressDao(relayAddressDao);

        Thread mailProcessorThread = new Thread(app.mailProcessor);
        mailProcessorThread.start();

        app.smtpServer = new SmtpServer(SMTP_SERVER_PORT);
        app.smtpServer.addObserver(app.mailProcessor);
        app.smtpServer.startServer();

        app.apiServer = new ApiServer(API_SERVER_PORT);
        app.apiServer.setSmtpServer(app.smtpServer);
        app.apiServer.setMailProcessor(app.mailProcessor);
        app.apiServer.setMailMessageDao(mailMessageDao);
        app.apiServer.setRelayAddressDao(relayAddressDao);
        app.apiServer.start();

        System.out.println("Type EXIT to quit");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(!reader.readLine().equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to quit");
        }

        app.mailProcessor.stop();
        app.smtpServer.stop();
        app.apiServer.stop();
    }
}
