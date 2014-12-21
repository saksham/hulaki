package com.dumbster.smtp.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SmtpServer {
    private final int port;
    private boolean stopped;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;


    public SmtpServer(int port) {
        this.port = port;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public void run() throws Exception {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new SmtpServerInitializer());
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);


            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            stop();
        }
    }

    public void stop() {
        if(stopped) {
            return;
        }
        stopped = true;
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 2500;
        }
        new SmtpServer(port).run();
    }
}
