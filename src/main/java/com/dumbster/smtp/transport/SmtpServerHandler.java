package com.dumbster.smtp.transport;

import com.google.common.collect.Lists;
import io.netty.channel.*;
import org.apache.log4j.Logger;

import java.util.List;


public class SmtpServerHandler extends ChannelHandlerAdapter
        implements Observable<SmtpMessage> {
    private static final Logger logger = Logger.getLogger(SmtpServerHandler.class);


    private List<Observer<SmtpMessage>> observers = Lists.newArrayList();
    private SmtpState currentState = SmtpState.CONNECT;
    private SmtpMessage smtpMessage = new SmtpMessage();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("Connected!");
        SmtpRequest request = new SmtpRequest(currentState, SmtpCommand.CONNECT);
        SmtpResult result = request.execute();
        this.currentState = result.getNextState();
        ctx.writeAndFlush(result.toSmtpResponseString() + "\r\n");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String line = (String) msg;
        logger.info("CLIENT: " + line);

        SmtpResult result;
        if(currentState == SmtpState.DATA_HEADER || currentState == SmtpState.DATA_BODY) {
            result = onData(line);
        } else {
            result = onCommand(line);
        }

        if(result != null) {
            currentState = result.getNextState();
            if (result.getSmtpResponse() != null) {
                ChannelFuture f = ctx.writeAndFlush(result.toSmtpResponseString() + "\r\n");
                if(currentState == SmtpState.QUIT) {
                    f.addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    private SmtpResult onCommand(String line) {
        SmtpCommand command = SmtpCommand.parse(line);
        SmtpRequest request = new SmtpRequest(currentState, command);
        return request.execute();
    }

    private SmtpResult onData(String line) {
        SmtpResult executionResult = null;
        if (line.trim().equals(SmtpCommand.DATA_END.getCommand())) {
            smtpMessage.close();
            notifyObservers(smtpMessage);
            smtpMessage = new SmtpMessage();
            SmtpRequest request = new SmtpRequest(currentState, SmtpCommand.DATA_END);
            executionResult = request.execute();
        } else {
            if (line.startsWith("..")) {
                line = line.substring(1);
            }
            smtpMessage.addLine(line);
        }
        return executionResult;
    }

    private void notifyObservers(SmtpMessage smtpMessage) {
        for(Observer<SmtpMessage> observer : observers) {
            observer.added(smtpMessage);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Completed reading data");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("An error occurred while processing the request", cause);
        ctx.channel().close();
    }

    @Override
    public void addObserver(Observer<SmtpMessage> observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<SmtpMessage> observer) {
        this.observers.remove(observer);
    }
}
