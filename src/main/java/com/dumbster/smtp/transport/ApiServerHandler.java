package com.dumbster.smtp.transport;


import com.dumbster.smtp.api.*;
import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.exceptions.ApiProtocolException;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ApiServerHandler {
    private static final Logger logger = Logger.getLogger(ApiServerHandler.class);
    private MailMessageDao mailMessageDao;
    private RelayAddressDao relayAddressDao;
    private MailProcessor mailProcessor;
    private SmtpServer smtpServer;

    public void setMailMessageDao(MailMessageDao mailMessageDao) {
        this.mailMessageDao = mailMessageDao;
    }

    public void setRelayAddressDao(RelayAddressDao relayAddressDao) {
        this.relayAddressDao = relayAddressDao;
    }

    public void setMailProcessor(MailProcessor mailProcessor) {
        this.mailProcessor = mailProcessor;
    }

    public void setSmtpServer(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void processRequest(Socket clientSocket) {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String messageFromClient = inFromClient.readLine();
            logger.info("Client: " + messageFromClient);

            ApiRequest request = parseRequest(messageFromClient);

            ApiResponse response;
            if (request == null || request.getCommand() == ApiCommand.INVALID) {
                response = new StatusResponse(404, "Invalid command!");
            } else if (request.getCommand() == ApiCommand.COUNT) {
                response = process((CountRequest) request);
            } else if (request.getCommand() == ApiCommand.GET) {
                response = process((GetRequest) request);
            } else if (request.getCommand() == ApiCommand.CLEAR) {
                response = process((ClearRequest) request);
            } else if (request.getCommand() == ApiCommand.RELAY) {
                response = process((RelayRequest) request);
            } else if (request.getCommand() == ApiCommand.SERVER_STATUS) {
                response = process((ServerStatusRequest) request);
            } else {
                response = new StatusResponse(403, "Bad request!");
            }
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
            response.marshalResponse(outputStream);
            outputStream.close();
            clientSocket.close();

        } catch (IOException ioe) {
            throw new ApiProtocolException(ioe);
        }
    }

    private ApiRequest parseRequest(String messageFromClient) {
        ApiRequest request = null;
        try {
            request = ApiRequest.fromRequestString(messageFromClient);
        } catch (ApiProtocolException ex) {
            logger.warn("Failed to parse request from client: " + messageFromClient);
        }
        return request;
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
            status = (!this.smtpServer.isStopped()) ? ServerStatus.RUNNING : ServerStatus.STOPPED;
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
