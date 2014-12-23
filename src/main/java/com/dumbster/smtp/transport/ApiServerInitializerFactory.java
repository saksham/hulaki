package com.dumbster.smtp.transport;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

public class ApiServerInitializerFactory {
    
    private ApiServerHandlerFactory apiServerHandlerFactory;

    private ApiServerInitializerFactory() {
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
