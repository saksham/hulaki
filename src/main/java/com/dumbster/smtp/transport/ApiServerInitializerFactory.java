package com.dumbster.smtp.transport;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

public class ApiServerInitializerFactory {
    private static ApiServerInitializerFactory instance = null;
    
    private ApiServerHandlerFactory apiServerHandlerFactory;

    private ApiServerInitializerFactory() {
    }

    public static ApiServerInitializerFactory getInstance() {
        if (instance == null) {
            synchronized (ApiServerInitializerFactory.class) {
                if (instance == null) {
                    instance = new ApiServerInitializerFactory();
                }
            }
        }
        return instance;
    }

    public ApiServerInitializer create() {
        Assert.notNull(this.apiServerHandlerFactory);
        
        ApiServerInitializer apiServerInitializer = new ApiServerInitializer();
        apiServerInitializer.setServerHandlerFactory(this.apiServerHandlerFactory);
        return apiServerInitializer;
    }
    
    @Required
    public void setApiServerHandlerFactory(ApiServerHandlerFactory apiServerHandlerFactory) {
        this.apiServerHandlerFactory = apiServerHandlerFactory;
    }
}
