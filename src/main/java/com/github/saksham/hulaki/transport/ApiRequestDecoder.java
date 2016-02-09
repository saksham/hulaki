package com.github.saksham.hulaki.transport;

import com.github.saksham.hulaki.api.ApiRequest;
import com.github.saksham.hulaki.api.InvalidRequest;
import com.github.saksham.hulaki.exceptions.ApiProtocolException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ApiRequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final Logger logger = LogManager.getLogger(ApiRequestDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        String messageFromClient = byteBuf.toString(Charsets.UTF_8);
        ApiRequest request = new InvalidRequest(messageFromClient);
        try {
            request = ApiRequest.fromRequestString(messageFromClient);
            logger.info("CLIENT: " + request.toRequestString());
        } catch (ApiProtocolException ex) {
            logger.warn("Failed to parse request from client: " + messageFromClient);
        }
        list.add(request);
    }
}
