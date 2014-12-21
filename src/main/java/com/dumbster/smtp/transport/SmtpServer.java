package com.dumbster.smtp.transport;

import com.dumbster.smtp.exceptions.SmtpException;
import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import java.util.List;

public class SmtpServer implements Runnable, Observable<SmtpMessage>, Observer<SmtpMessage> {
    private static final Logger logger = Logger.getLogger(SmtpServer.class);
    private final int port;
    private boolean stopped;
    private List<Observer<SmtpMessage>> observers;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;


    public SmtpServer(int port) {
        this.port = port;
        this.stopped = true;
        this.observers = Lists.newArrayList();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 2500;
        }
        SmtpServer.start(port);
        System.in.read();
    }

    public static SmtpServer start(int port) throws Exception {
        SmtpServer smtpServer = new SmtpServer(port);
        logger.info("Starting SMTP server on port: " + port + "...");
        Thread smtpServerThread = new Thread(smtpServer);

        synchronized (smtpServer) {
            smtpServerThread.start();
            smtpServer.wait();
        }
        Assert.isTrue(!smtpServer.isStopped());
        return smtpServer;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void run() {
        try {
            start();
        } catch (Exception ex) {
            throw new SmtpException(ex);
        }
    }

    private void start() throws Exception {
        if (!stopped) {
            return;
        }

        stopped = false;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new SmtpServerInitializer(this));
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            f.addListener(new SmtpServerBoundListener(this));
            f.channel().closeFuture().sync();
        } finally {
            stop();
        }
    }

    public void stop() {
        if (stopped) {
            return;
        }
        stopped = true;
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
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
