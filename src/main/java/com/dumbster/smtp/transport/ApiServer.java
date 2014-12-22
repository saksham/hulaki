package com.dumbster.smtp.transport;

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.storage.InMemoryMailMessageDao;
import com.dumbster.smtp.storage.InMemoryRelayAddressDao;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class ApiServer {
    private static final Logger logger = Logger.getLogger(ApiServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int port;


    @Autowired
    private SmtpServer smtpServer;

    @Autowired
    private MailProcessor mailProcessor;

    @Autowired
    private MailMessageDao mailMessageDao;

    @Autowired
    private RelayAddressDao relayAddressDao;

    public ApiServer(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup(10);
        this.workerGroup = new NioEventLoopGroup(20);
    }

    public boolean isStopped() {
        return this.bossGroup.isShutdown() && this.workerGroup.isShutdown();
    }

    public void start() throws InterruptedException {
        logger.info("Starting API server on port: " + port + "...");
        ApiServerInitializer initializer = new ApiServerInitializer();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childHandler(initializer);
        b.option(ChannelOption.SO_BACKLOG, 128);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);

        b.bind(port).sync();
        logger.info("Started API server!");
    }

    public void stop() throws Exception {
        logger.info("Stopping API server...");
        Future<?> fw = workerGroup.shutdownGracefully();
        Future<?> fb = bossGroup.shutdownGracefully();
        fw.await();
        fb.await();
        logger.info("Stopped API server!");
    }

    public static void main(String[] args) throws Exception {
        final int port = 6869;
        ApiServer server = new ApiServer(port);

        System.out.println("Type EXIT to quit");
        server.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!reader.readLine().equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to quit");
        }
        server.stop();
    }

    public void setMailMessageDao(MailMessageDao mailMessageDao) {
        this.mailMessageDao = mailMessageDao;
    }

    public void setRelayAddressDao(RelayAddressDao relayAddressDao) {
        this.relayAddressDao = relayAddressDao;
    }
}
