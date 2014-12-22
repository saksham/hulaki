package com.dumbster.smtp.transport;

import com.dumbster.smtp.api.ApiRequest;
import com.dumbster.smtp.exceptions.ApiProtocolException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.List;

public class ApiRequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final Logger logger = Logger.getLogger(ApiRequestDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        String messageFromClient = byteBuf.toString(Charset.defaultCharset());
        ApiRequest request = null;
        try {
            request = ApiRequest.fromRequestString(messageFromClient);
            logger.info("CLIENT: " + request.toRequestString());
        } catch (ApiProtocolException ex) {
            logger.warn("Failed to parse request from client: " + messageFromClient);
        }
        list.add(request);
    }
}
