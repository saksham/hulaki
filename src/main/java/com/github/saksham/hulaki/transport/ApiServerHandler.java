package com.github.saksham.hulaki.transport;


import com.github.saksham.hulaki.app.MailProcessor;
import com.github.saksham.hulaki.exceptions.ApiProtocolException;
import com.github.saksham.hulaki.storage.MailMessageDao;
import com.github.saksham.hulaki.storage.RelayAddressDao;
import com.github.saksham.hulaki.api.ApiRequest;
import com.github.saksham.hulaki.api.ApiResponse;
import com.github.saksham.hulaki.api.ClearRequest;
import com.github.saksham.hulaki.api.CountRequest;
import com.github.saksham.hulaki.api.CountResponse;
import com.github.saksham.hulaki.api.GetRequest;
import com.github.saksham.hulaki.api.GetResponse;
import com.github.saksham.hulaki.api.InvalidRequest;
import com.github.saksham.hulaki.api.RelayMode;
import com.github.saksham.hulaki.api.RelayRequest;
import com.github.saksham.hulaki.api.RelayResponse;
import com.github.saksham.hulaki.api.ServerName;
import com.github.saksham.hulaki.api.ServerStatus;
import com.github.saksham.hulaki.api.ServerStatusRequest;
import com.github.saksham.hulaki.api.StatusResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.springframework.util.Assert;


public class ApiServerHandler extends ChannelHandlerAdapter {
    
    private final MailMessageDao mailMessageDao;
    private final RelayAddressDao relayAddressDao;
    private final MailProcessor mailProcessor;
    private final SmtpServer smtpServer;
    
    public ApiServerHandler(MailMessageDao mailMessageDao, RelayAddressDao relayAddressDao, MailProcessor mailProcessor, SmtpServer smtpServer) {
        Assert.notNull(mailMessageDao);
        Assert.notNull(relayAddressDao);
        Assert.notNull(mailProcessor);
        Assert.notNull(smtpServer);
        
        this.mailMessageDao = mailMessageDao;
        this.relayAddressDao = relayAddressDao;
        this.mailProcessor = mailProcessor;
        this.smtpServer = smtpServer;
    }
    
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ApiRequest request = (ApiRequest) msg;
        ChannelFuture writeHandler = null;

        try {
            ApiResponse response;
            if (request == null || request instanceof InvalidRequest) {
                response = new StatusResponse(404, "Invalid command!");
            } else if (request instanceof CountRequest) {
                response = process((CountRequest) request);
            } else if (request instanceof GetRequest) {
                response = process((GetRequest) request);
            } else if (request instanceof ClearRequest) {
                response = process((ClearRequest) request);
            } else if (request instanceof RelayRequest) {
                response = process((RelayRequest) request);
            } else if (request instanceof ServerStatusRequest) {
                response = process((ServerStatusRequest) request);
            } else {
                response = new StatusResponse(403, "Bad request!");
            }

            writeHandler = ctx.writeAndFlush(response);
        } finally {
            ReferenceCountUtil.release(msg);
            if(writeHandler != null) {
                writeHandler.addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.close();
            }
        }
    }

    private ApiResponse process(CountRequest countRequest) {
        int cnt;
        if (countRequest.getRecipient() != null) {
            cnt = mailMessageDao.countMessagesForRecipient(countRequest.getRecipient());
        } else {
            cnt = mailMessageDao.countAllMessagesReceived();
        }
        return new CountResponse(countRequest.getRecipient(), cnt);
    }

    private ApiResponse process(GetRequest getRequest) {
        return new GetResponse(getRequest.getRecipient(), mailMessageDao.retrieveMessages(getRequest.getRecipient()));
    }

    private ApiResponse process(ClearRequest clearRequest) {
        mailMessageDao.clearMessagesForRecipient(clearRequest.getRecipient());
        return new StatusResponse(200, "OK");
    }

    private ApiResponse process(ServerStatusRequest request) {
        ServerStatus status;
        if (request.getServerName() == ServerName.MAIL_PROCESSOR) {
            status = (!this.mailProcessor.isStopped()) ? ServerStatus.RUNNING : ServerStatus.STOPPED;
        } else {
            status = (this.smtpServer.isRunning()) ? ServerStatus.RUNNING : ServerStatus.STOPPED;
        }
        return new StatusResponse(status.getStatus(), status.getStatusString());
    }

    private ApiResponse process(RelayRequest relayRequest) {
        if (relayRequest.getRecipient() != null) {
            if (relayRequest.getRelayMode() == RelayMode.ADD) {
                relayAddressDao.addRelayRecipient(relayRequest.getRecipient());
                return new StatusResponse(200, "OK");
            } else if (relayRequest.getRelayMode() == RelayMode.REMOVE) {
                relayAddressDao.removeRelayRecipient(relayRequest.getRecipient());
                return new StatusResponse(200, "OK");
            } else if (relayRequest.getRelayMode() == RelayMode.GET) {
                if (relayAddressDao.isRelayRecipient(relayRequest.getRecipient())) {
                    return new StatusResponse(200, "OK");
                } else {
                    return new StatusResponse(404, "NOT FOUND");
                }
            }
        } else {
            if (relayRequest.getRelayMode() == RelayMode.REMOVE) {
                relayAddressDao.clearRelayRecipients();
                return new StatusResponse(200, "OK");
            } else if (relayRequest.getRelayMode() == RelayMode.GET) {
                return new RelayResponse(relayAddressDao.getRelayRecipients());
            }
        }
        throw new ApiProtocolException("Parameters missing in the request.");
    }
}
