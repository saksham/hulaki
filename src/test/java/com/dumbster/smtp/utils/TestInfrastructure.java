/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dumbster.smtp.utils;

import com.dumbster.smtp.app.MailProcessor;
import com.dumbster.smtp.storage.MailMessageDao;
import com.dumbster.smtp.storage.RelayAddressDao;
import com.dumbster.smtp.transport.ApiServer;
import com.dumbster.smtp.transport.ApiServerHandlerFactory;
import com.dumbster.smtp.transport.ApiServerInitializerFactory;
import com.dumbster.smtp.transport.Observer;
import com.dumbster.smtp.transport.SmtpMessage;
import com.dumbster.smtp.transport.SmtpServer;
import org.mockito.Mockito;

import static org.mockito.Mockito.reset;


public class TestInfrastructure {
    public static final String SMTP_HOSTNAME = "localhost";
    public static final int SMTP_PORT = 2500;
    public static final String API_HOSTNAME = "localhost";
    public static final int API_PORT = 6869;
    private final SmtpServer smtpServer;
    private final ApiServer apiServer;
    private final MailProcessor mailProcessor;
    private final MailMessageDao mailMessageDao;
    private final RelayAddressDao relayAddressDao;

    @SuppressWarnings("unchecked")
    private Observer<SmtpMessage> smtpMessageObserver = Mockito.mock(Observer.class);

    public TestInfrastructure() {
        this(Mockito.mock(MailMessageDao.class));
    }
    
    public TestInfrastructure(MailMessageDao mailMessageDao) {
        this(mailMessageDao, Mockito.mock(RelayAddressDao.class));
    }
    
    public TestInfrastructure(MailMessageDao mailMessageDao, RelayAddressDao relayAddressDao) {
        this.smtpServer = new SmtpServer(SMTP_PORT);
        this.apiServer = new ApiServer(API_PORT);
        this.mailProcessor = new MailProcessor();
        this.smtpServer.addObserver(this.mailProcessor);
        this.smtpServer.addObserver(smtpMessageObserver);

        this.mailMessageDao = mailMessageDao;
        this.relayAddressDao = relayAddressDao;
        this.mailProcessor.setMailMessageDao(this.mailMessageDao);
        this.mailProcessor.setRelayAddressDao(this.relayAddressDao);

        ApiServerHandlerFactory apiServerHandlerFactory = new ApiServerHandlerFactory(this.mailMessageDao, this.relayAddressDao, this.mailProcessor, this.smtpServer);
        ApiServerInitializerFactory serverInitializerFactory = new ApiServerInitializerFactory(apiServerHandlerFactory);
        this.apiServer.setApiServerHandlerFactory(apiServerHandlerFactory);
        this.apiServer.setApiServerInitializerFactory(serverInitializerFactory);
    }

    public void startApiServer() throws Exception {
        apiServer.start();
    }

    public void startSmtpServer() throws Exception {
        smtpServer.start();
    }

    public void startMailProcessor() throws Exception {
        Thread mailProcessorThread = new Thread(mailProcessor);
        mailProcessorThread.start();
    }

    public void stop() throws Exception {
        apiServer.stop();
        smtpServer.stop();
        mailProcessor.stop();
    }

    public MailMessageDao getMailMessageDao() {
        return mailMessageDao;
    }

    public SmtpServer getSmtpServer() {
        return smtpServer;
    }

    public ApiServer getApiServer() {
        return apiServer;
    }

    public Observer<SmtpMessage> getSmtpMessageObserver() {
        return smtpMessageObserver;
    }
    
    @SuppressWarnings("unchecked")
    public void resetSmtpMessageObserverMock() {
        reset(this.smtpMessageObserver);
    }
}
