package com.hulaki.smtp.transport;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;


public class SmtpServerInitializer extends ChannelInitializer<SocketChannel> {
    private SmtpServer smtpServer;

    public SmtpServerInitializer(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1000, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast("encoder", new StringEncoder());

        SmtpServerHandler serverHandler = new SmtpServerHandler();
        serverHandler.addObserver(this.smtpServer);
        pipeline.addLast("handler", serverHandler);
    }


}
