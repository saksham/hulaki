package com.dumbster.smtp.app;

import com.dumbster.smtp.storage.MailMessageDaoFactory;
import com.dumbster.smtp.transport.ApiServer;
import com.dumbster.smtp.transport.SmtpServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


public class ServerApplication implements ApplicationListener<ContextStartedEvent> {

    

    public static void main(String[] args) throws Exception {
        setupMailMessageDaoFactory();
        
        ClassLoader classLoader = ServerApplication.class.getClassLoader();
        URL appConfigResource = classLoader.getResource("application-config.xml");
        assert appConfigResource != null;

        ApplicationContext context = new FileSystemXmlApplicationContext(appConfigResource.getFile());

        ApiServer apiServer = context.getBean(ApiServer.class);
        apiServer.start();
        SmtpServer smtpServer = context.getBean(SmtpServer.class);
        smtpServer.start();

        MailProcessor mailProcessor = context.getBean(MailProcessor.class);
        smtpServer.addObserver(mailProcessor);
        Thread mailProcessorThread = new Thread(mailProcessor);
        mailProcessorThread.start();
        
        System.out.println("Type EXIT to quit");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!reader.readLine().equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to quit");
        }

        apiServer.stop();
        smtpServer.stop();
        mailProcessor.stop();

    }

    private static void setupMailMessageDaoFactory() {
        String storageModeProp = System.getProperty("smtp.storage.mode");
        MailMessageDaoFactory.StorageMode storageMode = MailMessageDaoFactory.StorageMode.IN_MEMORY;
        if(!StringUtils.isEmpty(storageModeProp)) {
            storageMode = MailMessageDaoFactory.StorageMode.valueOf(storageModeProp.toUpperCase());
        }
        MailMessageDaoFactory.setStorageMode(storageMode);
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {

    }
}
