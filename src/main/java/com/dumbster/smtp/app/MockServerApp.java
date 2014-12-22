package com.dumbster.smtp.app;

import com.dumbster.smtp.storage.InMemoryMailMessageDao;
import com.dumbster.smtp.storage.InMemoryRelayAddressDao;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import com.dumbster.smtp.transport.ApiServer;
import com.dumbster.smtp.transport.SmtpServer;
import com.dumbster.smtp.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Configuration
@ComponentScan(basePackages = {"com.dumbster.smtp.transport", "com.dumbster.smtp.transport"})
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

    public MockServerApp() {
        apiServer = new ApiServer(API_SERVER_PORT);
        smtpServer = new SmtpServer(SMTP_SERVER_PORT);
        mailProcessor = new MailProcessor();
        mailMessageDao = new InMemoryMailMessageDao();
        relayAddressDao = new InMemoryRelayAddressDao();
        emailSender = new EmailSender(SMTP_RELAY_HOSTNAME, SMTP_RELAY_PORT);
    }

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


    @Bean
    public ApiServer getApiServer() {
        return apiServer;
    }

    @Bean
    public MailProcessor getMailProcessor() {
        return mailProcessor;
    }

    @Bean
    public SmtpServer getSmtpServer() {
        return smtpServer;
    }

    @Bean
    public MailMessageDao getMailMessageDao() {
        return mailMessageDao;
    }

    @Bean
    public RelayAddressDao getRelayAddressDao() {
        return relayAddressDao;
    }

    @Bean
    public EmailSender getEmailSender() {
        return emailSender;
    }
}
