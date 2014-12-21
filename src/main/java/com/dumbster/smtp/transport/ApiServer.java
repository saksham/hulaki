package com.dumbster.smtp.transport;

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.exceptions.ApiException;
import com.dumbster.smtp.storage.IMailStorage;
import com.dumbster.smtp.storage.IRelayAddressStorage;
import com.dumbster.smtp.storage.InMemoryMailStorage;
import com.dumbster.smtp.storage.InMemoryRelayAddressStorage;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ApiServer implements Runnable {

    public static final long TIMEOUT = 2000L;
    private static final Logger logger = Logger.getLogger(ApiServer.class);
    private volatile boolean stopped = true;
    private ServerSocketChannel serverSocketChannel;
    private int port;


    private SmtpServer smtpServer;
    private MailProcessor mailProcessor;
    private IMailStorage mailStorage;
    private IRelayAddressStorage relayAddressStorage;

    public ApiServer(int port) {
        this.port = port;
    }

    public void setSmtpServer(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void setMailProcessor(MailProcessor mailProcessor) {
        this.mailProcessor = mailProcessor;
    }

    public void setMailStorage(IMailStorage mailStorage) {
        this.mailStorage = mailStorage;
    }

    public void setRelayAddressStorage(IRelayAddressStorage relayAddressStorage) {
        this.relayAddressStorage = relayAddressStorage;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void startAndWait() throws InterruptedException {
        Thread apiServerThread = new Thread(this);
        apiServerThread.start();
        synchronized (this) {
            this.wait(TIMEOUT);
            if(stopped) {
                throw new ApiException("Couldn't start the server.");
            }
        }

    }

    private void start() throws Exception {
        synchronized (this) {
            if(stopped) {
                stopped = false;
                try {
                    openServerSocketChannel();
                    logger.info("Started!");
                } catch (IOException ex) {
                    this.stopped = true;
                    throw ex;
                } finally {
                    this.notifyAll();
                }
            }
        }
    }

    public void stop() throws Exception {
        synchronized (this) {
            if (!stopped) {
                logger.info("Stopping server...");
                stopped = true;
                this.wait(TIMEOUT);
            } else {
                logger.warn("Server was already stopped!");
            }
        }
    }


    @Override
    public void run() {
        if (!stopped) {
            logger.warn("Server already started. So, it won't be started again.");
            return;
        }
        try {
            logger.info("Starting server...");
            start();
            while (!stopped) {

                SocketChannel sc = serverSocketChannel.accept();
                if (sc != null) {
                    processMockRequestAsync(sc);
                } else {
                    Thread.sleep(100);
                }
            }
            synchronized (this) {
                serverSocketChannel.close();
                this.notifyAll();
                logger.info("Stopped!");
            }
        } catch (Exception ex) {
            throw new ApiException(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        final int port = 6869;
        ApiServer server = new ApiServer(port);
        server.setRelayAddressStorage(new InMemoryRelayAddressStorage());
        server.setMailStorage(new InMemoryMailStorage());

        System.out.println("Type EXIT to quit");
        server.startAndWait();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(!reader.readLine().equalsIgnoreCase("EXIT")) {
            System.out.println("Type EXIT to quit");
        }
        server.stop();
    }

    private void openServerSocketChannel() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
        serverSocketChannel.configureBlocking(false);
    }

    private void processMockRequestAsync(SocketChannel sc) {
        final Socket connectionSocket = sc.socket();
        Thread processorThread = new Thread() {
            public void run() {
                ApiServerHandler handler = new ApiServerHandler();
                handler.setMailStorage(mailStorage);
                handler.setRelayAddressStorage(relayAddressStorage);
                handler.setMailProcessor(mailProcessor);
                handler.setSmtpServer(smtpServer);
                handler.processRequest(connectionSocket);
            }
        };
        processorThread.start();
    }
}
