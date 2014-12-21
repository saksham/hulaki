package com.dumbster.smtp.transport;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
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
        logger.debug("CLIENT: " + line);

        SmtpResult result = executeCommand(line);
        SmtpState prevState = currentState;
        currentState = result.getNextState();

        if (prevState == SmtpState.DATA_HEADER || prevState == SmtpState.DATA_BODY) {
            onData(line, prevState);
        }


        if (result.getSmtpResponse() != null) {
            ctx.writeAndFlush(result.toSmtpResponseString() + "\r\n");
        }
    }

    private SmtpResult executeCommand(String line) {
        SmtpCommand command = SmtpCommand.parse(line);
        SmtpRequest request = new SmtpRequest(currentState, command);
        return request.execute();
    }

    private void onData(String line, SmtpState prevState) {
        if (prevState == SmtpState.DATA_BODY && currentState == SmtpState.QUIT) {
            smtpMessage.close();
            notifyObservers(smtpMessage);
            smtpMessage = new SmtpMessage();
        }
        if (line.startsWith("..")) {
            line = line.substring(1);
        }
        smtpMessage.addLine(line);
    }

    private void notifyObservers(SmtpMessage smtpMessage) {
        for (Observer<SmtpMessage> observer : observers) {
            observer.notify(smtpMessage);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Completed reading data");
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.observers.clear();
        this.observers = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("An error occurred while processing the request", cause);
        this.observers.clear();
        this.observers = null;
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
