package com.dumbster.smtp.transport;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.util.List;


public class SmtpServer implements Observable<SmtpMessage>, Observer<SmtpMessage>, Server {
    private static final Logger logger = Logger.getLogger(SmtpServer.class);

    private static final int BOSS_GROUP_THREAD_COUNT = 10;
    private static final int WORKER_GROUP_THREAD_COUNT = 20;
    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private volatile boolean started = false;
    private List<Observer<SmtpMessage>> observers;


    public SmtpServer(int port) {
        this.port = port;
        this.observers = Lists.newArrayList();
    }

    @Override
    public boolean isRunning() {
        return started && (!this.bossGroup.isShutdown() && !this.bossGroup.isShuttingDown()) &&
                (!this.workerGroup.isShutdown() && !this.workerGroup.isShuttingDown());
    }

    @Override
    public synchronized void start() throws InterruptedException {
        if (started) {
            logger.warn("SMTP Server already started.");
            return;
        }

        started = true;
        logger.info("Starting SMTP server on port: " + port + "...");
        ServerBootstrap b = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(BOSS_GROUP_THREAD_COUNT);
        workerGroup = new NioEventLoopGroup(WORKER_GROUP_THREAD_COUNT);
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childHandler(new SmtpServerInitializer(this));
        b.option(ChannelOption.SO_BACKLOG, 128);
        b.childOption(ChannelOption.SO_KEEPALIVE, false);

        b.bind(port).sync();
        logger.info("Started SMTP server!");
    }

    @Override
    public synchronized void stop() throws InterruptedException {
        if (!started) {
            logger.warn("SMTP Server already stopped.");
            return;
        }
        started = false;
        logger.info("Stopping SMTP server...");
        workerGroup.shutdownGracefully().sync();
        bossGroup.shutdownGracefully().sync();
        logger.info("Stopped SMTP server!");
    }

    @Override
    public void notify(SmtpMessage data) {
        for (Observer<SmtpMessage> observer : observers) {
            observer.notify(data);
        }
    }

    @Override
    public void addObserver(Observer<SmtpMessage> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<SmtpMessage> observer) {
        observers.remove(observer);
    }
}
