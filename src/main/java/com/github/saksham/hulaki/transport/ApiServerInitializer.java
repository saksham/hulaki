package com.github.saksham.hulaki.transport;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import org.springframework.beans.factory.annotation.Required;

public class ApiServerInitializer extends ChannelInitializer<SocketChannel> {

    private ApiServerHandlerFactory serverHandlerFactory;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1000, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new ApiRequestDecoder());
        pipeline.addLast("encoder", new ApiResponseEncoder());
        pipeline.addLast("handler", serverHandlerFactory.create());
    }

    @Required
    public void setServerHandlerFactory(ApiServerHandlerFactory serverHandlerFactory) {
        this.serverHandlerFactory = serverHandlerFactory;
    }
}
