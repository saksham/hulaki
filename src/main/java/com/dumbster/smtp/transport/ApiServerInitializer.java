package com.dumbster.smtp.transport;

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class ApiServerInitializer extends ChannelInitializer<SocketChannel> {

    private SmtpServer smtpServer;
    private MailProcessor mailProcessor;
    private MailMessageDao mailMessageDao;
    private RelayAddressDao relayAddressDao;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1000, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast("encoder", new StringEncoder());

        ApiServerHandler handler = new ApiServerHandler();
        handler.setMailMessageDao(mailMessageDao);
        handler.setRelayAddressDao(relayAddressDao);
        handler.setMailProcessor(mailProcessor);
        handler.setSmtpServer(smtpServer);
        pipeline.addLast("handler", handler);
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
}
