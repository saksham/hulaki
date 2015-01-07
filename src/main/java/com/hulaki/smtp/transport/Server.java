package com.hulaki.smtp.transport;

public interface Server {
    void start() throws Exception;
    void stop() throws Exception;
    boolean isRunning();
}
