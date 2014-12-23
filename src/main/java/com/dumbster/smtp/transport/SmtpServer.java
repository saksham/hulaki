package com.dumbster.smtp.transport;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


public class SmtpServer implements Observable<SmtpMessage>, Observer<SmtpMessage> {
    private static final Logger logger = Logger.getLogger(SmtpServer.class);
    private final int port;
    private volatile boolean running = false;
    private List<Observer<SmtpMessage>> observers;
    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;


    public SmtpServer(int port) {
        this.port = port;
        this.observers = Lists.newArrayList();
        this.bossGroup = new NioEventLoopGroup(10);
        this.workerGroup = new NioEventLoopGroup(20);
    }

    public static void main(String[] args) throws Exception {
        int port = 2500;

        System.out.println("Type EXIT to exit the program.");
        SmtpServer smtpServer = new SmtpServer(port);
        smtpServer.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!reader.readLine().equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to exit the program.");
        }
        smtpServer.stop();
    }

    public boolean isRunning() {
        return running && (!this.bossGroup.isShutdown() && !this.bossGroup.isShuttingDown()) &&
                (!this.workerGroup.isShutdown() && !this.workerGroup.isShuttingDown());
    }

    public synchronized void start() throws Exception {
        if (running) {
            logger.warn("SMTP Server already started.");
            return;
        }

        logger.info("Starting SMTP server on port: " + port + "...");
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childHandler(new SmtpServerInitializer(this));
        b.option(ChannelOption.SO_BACKLOG, 128);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);

        b.bind(port).sync();
        logger.info("Started SMTP server!");
        running = true;
    }

    public synchronized void stop() throws Exception {
        if (!running) {
            logger.warn("SMTP Server already stopped.");
            return;
        }
        running = false;
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
