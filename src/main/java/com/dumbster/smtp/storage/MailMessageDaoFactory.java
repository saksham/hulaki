package com.dumbster.smtp.storage;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class MailMessageDaoFactory {
    public static final String CONFIG_SMTP_STORAGE_FILE_FILEPATH = "smtp.storage.file.filepath";
    public static final String CONFIG_SMTP_STORAGE_SQLITE_DBFILEPATH = "smtp.storage.sqlite.filepath";

    private static volatile MailMessageDaoFactory instance;
    private static StorageMode storageMode;
    private MailMessageDao dao;

    public static enum StorageMode {
        SQLITE,
        FILE_BASED,
        IN_MEMORY,
    }


    private MailMessageDaoFactory() {
        switch (storageMode) {
            case SQLITE:
                dao = new SqliteMailMessageDao(System.getProperty(CONFIG_SMTP_STORAGE_SQLITE_DBFILEPATH));
                break;
            case FILE_BASED:
                dao = new FileBasedMailMessageDao(System.getProperty(CONFIG_SMTP_STORAGE_FILE_FILEPATH));
                break;
            case IN_MEMORY:
                dao = new InMemoryMailMessageDao();
                break;
            default:
                throw new IllegalStateException("Storage mode not initialized correctly.");
        }
    }

    public static MailMessageDaoFactory getInstance() {
        if (instance == null) {
            synchronized (MailMessageDaoFactory.class) {
                if (instance == null) {
                    instance = new MailMessageDaoFactory();
                }
            }
        }
        return instance;
    }

    public MailMessageDao getDao() {
        return this.dao;
    }

    public static void setStorageMode(StorageMode mode) {
        storageMode = mode;
        switch (mode) {
            case FILE_BASED:
                Assert.notNull(System.getProperty(CONFIG_SMTP_STORAGE_FILE_FILEPATH));
                break;
            case SQLITE:
                Assert.notNull(System.getProperty(CONFIG_SMTP_STORAGE_SQLITE_DBFILEPATH));
                break;
            default:
                break;
        }
    }
}
