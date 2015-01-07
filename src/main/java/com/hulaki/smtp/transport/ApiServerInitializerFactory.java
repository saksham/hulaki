package com.hulaki.smtp.transport;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ApiServerInitializerFactory {
    
    private final ApiServerHandlerFactory apiServerHandlerFactory;

    public ApiServerInitializerFactory(ApiServerHandlerFactory apiServerHandlerFactory) {
        this.apiServerHandlerFactory = apiServerHandlerFactory;
    }

    public ApiServerInitializer create() {
        Assert.notNull(this.apiServerHandlerFactory);
        
        ApiServerInitializer apiServerInitializer = new ApiServerInitializer();
        apiServerInitializer.setServerHandlerFactory(this.apiServerHandlerFactory);
        return apiServerInitializer;
    }
}
