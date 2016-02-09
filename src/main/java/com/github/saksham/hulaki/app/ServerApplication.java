package com.github.saksham.hulaki.app;

import com.github.saksham.hulaki.storage.MailMessageDaoFactory;
import com.github.saksham.hulaki.transport.ApiServer;
import com.github.saksham.hulaki.transport.SmtpServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;


public class ServerApplication implements ApplicationListener<ContextStartedEvent> {

    private static final Logger logger = LogManager.getLogger(ServerApplication.class);
    public static final String CONFIG_STORAGE_MODE = "storage.mode";


    public static void main(String[] args) throws Exception {
        setupMailMessageDaoFactory();
        ApplicationContext context = new ClassPathXmlApplicationContext("application-config.xml");

        final ApiServer apiServer = context.getBean(ApiServer.class);
        final SmtpServer smtpServer = context.getBean(SmtpServer.class);
        final MailProcessor mailProcessor = context.getBean(MailProcessor.class);

        apiServer.start();
        smtpServer.start();

        smtpServer.addObserver(mailProcessor);
        Executors.newSingleThreadExecutor().execute(mailProcessor);

        waitForInterruption();

        apiServer.stop();
        smtpServer.stop();
        mailProcessor.stop();
    }

    private static void waitForInterruption() throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        if(reader != null) {
            // Started in interactive mode => close when user types in EXIT
            System.out.println("Type EXIT to close the application");
            String line = reader.readLine();
            while (!line.equalsIgnoreCase("EXIT")) {
                System.out.println("Type EXIT to close the application");
                line = reader.readLine();
            }
        } else {
            // Started in headless mode => wait forever
            while(true) {
                Thread.sleep(500L);
            }
        }
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
