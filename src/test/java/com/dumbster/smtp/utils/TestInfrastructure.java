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
import com.dumbster.smtp.transport.ApiServerHandler;
import com.dumbster.smtp.transport.ApiServerInitializer;
import com.dumbster.smtp.transport.SmtpServer;
import org.mockito.Mockito;

public class TestInfrastructure {
    private ApiServer apiServer;
    private SmtpServer smtpServer;
    private MailProcessor mailProcessor;
    private MailMessageDao mailMessageDao;
    private RelayAddressDao relayAddressDao;

    public TestInfrastructure(int smtpPort, int apiPort) {
        this.apiServer = new ApiServer(apiPort);
        this.smtpServer = new SmtpServer(smtpPort);
        this.mailProcessor = new MailProcessor();
        this.smtpServer.addObserver(this.mailProcessor);
        
        this.mailMessageDao = Mockito.mock(MailMessageDao.class);
        this.relayAddressDao = Mockito.mock(RelayAddressDao.class);
        this.mailProcessor.setMailMessageDao(this.mailMessageDao);
        this.mailProcessor.setRelayAddressDao(this.relayAddressDao);
    }

    public void ready() {
        apiServer.setApiServerInitializer(newApiServerInitializer());
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
    
    private ApiServerInitializer newApiServerInitializer() {
        ApiServerInitializer apiServerInitializer = new ApiServerInitializer();
        ApiServerHandler apiServerHandler = new ApiServerHandler();
        apiServerInitializer.setApiServerHandler(apiServerHandler);
        apiServerHandler.setMailMessageDao(mailMessageDao);
        apiServerHandler.setSmtpServer(smtpServer);
        apiServerHandler.setRelayAddressDao(relayAddressDao);
        return apiServerInitializer;
    }

    public void stop() throws Exception {
        apiServer.stop();
        smtpServer.stop();
        mailProcessor.stop();
    }

    public MailMessageDao getMailMessageDao() {
        return mailMessageDao;
    }

    public void setMailMessageDao(MailMessageDao mailMessageDao) {
        this.mailMessageDao = mailMessageDao;
        this.mailProcessor.setMailMessageDao(this.mailMessageDao);
    }
}
