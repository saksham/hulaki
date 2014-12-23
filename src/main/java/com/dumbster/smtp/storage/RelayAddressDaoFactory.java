package com.dumbster.smtp.storage;


public class RelayAddressDaoFactory {
    private static volatile RelayAddressDaoFactory instance = null;
    private RelayAddressDao dao;

    private RelayAddressDaoFactory() {
        dao = new InMemoryRelayAddressDao();
    }

    public static RelayAddressDaoFactory getInstance() {
        if (instance == null) {
            synchronized (RelayAddressDaoFactory.class) {
                if (instance == null) {
                    instance = new RelayAddressDaoFactory();
                }
            }
        }
        return instance;
    }

    public RelayAddressDao getDao() {
        return this.dao;
    }
}
