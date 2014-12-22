package com.dumbster.smtp.transport;

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.exceptions.ApiException;
import com.dumbster.smtp.storage.InMemoryMailMessageDao;
import com.dumbster.smtp.storage.InMemoryRelayAddressDao;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ApiServer implements Runnable {

    public static final long TIMEOUT = 2000L;
    private static final Logger logger = Logger.getLogger(ApiServer.class);
    private volatile boolean stopped = true;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int port;


    private SmtpServer smtpServer;
    private MailProcessor mailProcessor;
    private MailMessageDao mailMessageDao;
    private RelayAddressDao relayAddressDao;

    public ApiServer(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup(10);
        this.workerGroup = new NioEventLoopGroup(20);
    }

    public static void main(String[] args) throws Exception {
        final int port = 6869;
        ApiServer server = new ApiServer(port);
        server.setRelayAddressDao(new InMemoryRelayAddressDao());
        server.setMailMessageDao(new InMemoryMailMessageDao());

        System.out.println("Type EXIT to quit");
        server.startAndWait();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!reader.readLine().equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to quit");
        }
        server.stop();
    }

    public void startAndWait() throws InterruptedException {
        Thread apiServerThread = new Thread(this);
        apiServerThread.start();
        synchronized (this) {
            this.wait(TIMEOUT);
            if (stopped) {
                throw new ApiException("Couldn't start the server.");
            }
        }

    }

    private void start() throws Exception {
        synchronized (this) {
            if (stopped) {
                stopped = false;
                try {
                    bootstrapServer();
                    logger.info("Started!");
                } catch (Exception ex) {
                    this.stopped = true;
                    throw ex;
                } finally {
                    this.notifyAll();
                }
            }
        }
    }

    public void stop() throws Exception {
        synchronized (this) {
            if (!stopped) {
                logger.info("Stopping server...");
                stopped = true;
                this.wait(TIMEOUT);
                logger.info("Stopped!");
            } else {
                logger.warn("Server was already stopped!");
            }
        }
    }

    @Override
    public void run() {
        if (!stopped) {
            logger.warn("Server already started. So, it won't be started again.");
            return;
        }
        try {
            logger.info("Starting API server on port: " + port + "...");
            start();

            while (!stopped) {
                Thread.sleep(1000L);
            }

            synchronized (this) {
                final ApiServer thisServer = this;
                bossGroup.shutdownGracefully().addListener(new GenericFutureListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        workerGroup.shutdownGracefully().addListener(new GenericFutureListener() {
                            @Override
                            public void operationComplete(Future future) throws Exception {
                                synchronized (thisServer) {
                                    thisServer.notifyAll();
                                }
                            }
                        });
                    }
                });
            }
        } catch (Exception ex) {
            throw new ApiException(ex);
        }
    }

    private void bootstrapServer() throws InterruptedException {
        ApiServerInitializer initializer = new ApiServerInitializer();
        initializer.setSmtpServer(smtpServer);
        initializer.setMailProcessor(mailProcessor);
        initializer.setMailMessageDao(mailMessageDao);
        initializer.setRelayAddressDao(relayAddressDao);

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childHandler(initializer);
        b.option(ChannelOption.SO_BACKLOG, 128);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);

        b.bind(port).sync();
    }

    public void setSmtpServer(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void setMailProcessor(MailProcessor mailProcessor) {
        this.mailProcessor = mailProcessor;
    }

    public void setMailMessageDao(MailMessageDao mailMessageDao) {
        this.mailMessageDao = mailMessageDao;
    }

    public void setRelayAddressDao(RelayAddressDao relayAddressDao) {
        this.relayAddressDao = relayAddressDao;
    }

    public boolean isStopped() {
        return stopped;
    }
}
