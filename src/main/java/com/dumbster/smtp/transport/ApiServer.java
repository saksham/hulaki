package com.dumbster.smtp.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ApiServer implements Server {
    private static final Logger logger = Logger.getLogger(ApiServer.class);

    private static final int BOSS_GROUP_THREAD_COUNT = 10;
    private static final int WORKER_GROUP_THREAD_COUNT = 20;
    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private volatile boolean started = false;
    private ApiServerInitializerFactory apiServerInitializerFactory;
    private ApiServerHandlerFactory apiServerHandlerFactory;

    public ApiServer(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup(10);
        this.workerGroup = new NioEventLoopGroup(20);
    }

    @Override
    public boolean isRunning() {
        return started && (!this.bossGroup.isShutdown() && !this.bossGroup.isShuttingDown()) &&
                (!this.workerGroup.isShutdown() && !this.workerGroup.isShuttingDown());
    }

    @Override
    public synchronized void start() throws InterruptedException {
        Assert.notNull(this.apiServerInitializerFactory);
        Assert.notNull(this.apiServerHandlerFactory);

        if (started) {
            logger.warn("API Server already started.");
            return;
        }

        started = true;
        logger.info("Starting API server on port: " + port + "...");
        ServerBootstrap b = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(BOSS_GROUP_THREAD_COUNT);
        workerGroup = new NioEventLoopGroup(WORKER_GROUP_THREAD_COUNT);
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childHandler(apiServerInitializerFactory.create());
        b.option(ChannelOption.SO_BACKLOG, 128);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);

        b.bind(port).sync();
        logger.info("Started API server!");
    }

    @Override
    public synchronized void stop() throws InterruptedException {
        if (!started) {
            logger.warn("API Server already stopped.");
            return;
        }
        logger.info("Stopping API server...");
        workerGroup.shutdownGracefully();
        workerGroup.terminationFuture().sync();
        bossGroup.shutdownGracefully();
        bossGroup.terminationFuture().sync();
        logger.info("Stopped API server!");
        started = false;
    }


    @Required
    public void setApiServerInitializerFactory(ApiServerInitializerFactory apiServerInitializerFactory) {
        this.apiServerInitializerFactory = apiServerInitializerFactory;
    }

    @Required
    public void setApiServerHandlerFactory(ApiServerHandlerFactory apiServerHandlerFactory) {
        this.apiServerHandlerFactory = apiServerHandlerFactory;
    }
}
