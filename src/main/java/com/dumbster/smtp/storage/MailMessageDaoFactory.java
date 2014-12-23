package com.dumbster.smtp.storage;

public class MailMessageDaoFactory {
    private static volatile MailMessageDaoFactory instance = null;
    
    private MailMessageDao dao;

    
    private MailMessageDaoFactory() {
        dao = new InMemoryMailMessageDao();
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
}
