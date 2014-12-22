package com.dumbster.smtp.app;

import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import com.dumbster.smtp.transport.ApiServer;
import com.dumbster.smtp.transport.SmtpServer;
import com.dumbster.smtp.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Configuration
@ComponentScan
public class MockServerApp {
    public static final int SMTP_SERVER_PORT = 2500;
    public static final int API_SERVER_PORT = 6869;
    public static final String SMTP_RELAY_HOSTNAME = "gmail.com";
    public static final int SMTP_RELAY_PORT = 567;

    @Autowired
    private MailProcessor mailProcessor;

    @Autowired
    private ApiServer apiServer;

    @Autowired
    private SmtpServer smtpServer;

    @Autowired
    private MailMessageDao mailMessageDao;

    @Autowired
    private RelayAddressDao relayAddressDao;

    @Autowired
    private EmailSender emailSender;


    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(MockServerApp.class);

        ApiServer apiServer = context.getBean(ApiServer.class);
        apiServer.start();
        SmtpServer smtpServer = context.getBean(SmtpServer.class);
        smtpServer.start();

        System.out.println("Type EXIT to quit");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!reader.readLine().equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to quit");
        }

        apiServer.stop();
        smtpServer.stop();

    }
}
