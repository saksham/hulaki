package com.dumbster.smtp.transport;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

public class SmtpServerBoundListener implements ChannelFutureListener {
    private static final Logger logger = Logger.getLogger(SmtpServer.class);
    private final SmtpServer smtpServer;


    public SmtpServerBoundListener(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }


    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        synchronized (smtpServer) {
            logger.info("Started SMTP server!");
            smtpServer.notifyAll();
        }
    }
}
