package com.dumbster.smtp.app;

import com.dumbster.smtp.storage.MailMessageDaoFactory;
import com.dumbster.smtp.transport.ApiServer;
import com.dumbster.smtp.transport.SmtpServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ServerApplication implements ApplicationListener<ContextStartedEvent> {

    private static final Logger logger = Logger.getLogger(ServerApplication.class);
    public static final String CONFIG_STORAGE_MODE = "storage.mode";


    public static void main(String[] args) throws Exception {
        setupMailMessageDaoFactory();
        ApplicationContext context = new ClassPathXmlApplicationContext("application-config.xml");

        ApiServer apiServer = context.getBean(ApiServer.class);
        apiServer.start();
        SmtpServer smtpServer = context.getBean(SmtpServer.class);
        smtpServer.start();

        MailProcessor mailProcessor = context.getBean(MailProcessor.class);
        smtpServer.addObserver(mailProcessor);
        Thread mailProcessorThread = new Thread(mailProcessor);
        mailProcessorThread.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Type EXIT to close the application");
        String line = reader.readLine();
        while (!line.equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to close the application");
            line = reader.readLine();
        }
        apiServer.stop();
        smtpServer.stop();
        mailProcessor.stop();
    }

    private static void setupMailMessageDaoFactory() {
        String storageModeProp = System.getProperty(CONFIG_STORAGE_MODE);
        MailMessageDaoFactory.StorageMode storageMode;
        if (!StringUtils.isEmpty(storageModeProp)) {
            storageMode = MailMessageDaoFactory.StorageMode.valueOf(storageModeProp.toUpperCase());
            logger.info("Using " + storageMode.name() + " storage...");
        } else {
            storageMode = MailMessageDaoFactory.StorageMode.IN_MEMORY;
            logger.warn("No explicit storage mode specified. IN_MEMORY storage will be used.");
        }
        MailMessageDaoFactory.setStorageMode(storageMode);
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {

    }
}
