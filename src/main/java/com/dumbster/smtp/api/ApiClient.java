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

package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiException;
import com.dumbster.smtp.exceptions.SmtpTimeoutException;
import com.dumbster.smtp.utils.EmailUtils;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;

public class ApiClient implements IApiClient {
    private final Logger logger = Logger.getLogger(ApiClient.class);
    private final long DEFAULT_TIMEOUT_IN_MILLIS = 8000L;
    private final long MAX_NUM_OF_TIMES_TO_POLL = 8;
    private final String apiServerHostname;
    private final int apiServerPort;

    public ApiClient(String apiServerHostname, int apiServerPort) {
        this.apiServerHostname = apiServerHostname;
        this.apiServerPort = apiServerPort;
    }

    @Override
    public void waitForNewEmailOrFail(String recipient, int oldEmailCount) throws InterruptedException {
        waitForNewEmailOrFail(recipient, oldEmailCount, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    @Override
    public void waitForNewEmailOrFail(String recipient, int oldEmailCount, long timeoutInMillis) throws InterruptedException {

        long sleepTime = timeoutInMillis / MAX_NUM_OF_TIMES_TO_POLL;
        int newEmailCount = countMails(recipient);
        int pollCounter = 0;
        while (newEmailCount <= oldEmailCount && pollCounter < MAX_NUM_OF_TIMES_TO_POLL) {
            pollCounter++;
            Thread.sleep(sleepTime);
            newEmailCount = countMails(recipient);
        }
        if (newEmailCount <= oldEmailCount) {
            throw new SmtpTimeoutException("No new mail was received for " + recipient);
        }
    }

    @Override
    public int countMails() {
        return countMails(null);
    }

    @Override
    public int countMails(String recipient) {
        CountRequest countRequest = new CountRequest();
        if (recipient != null) {
            String normalizedEmail = EmailUtils.normalizeEmailAddress(recipient);
            countRequest.setRecipient(normalizedEmail);
        }
        String response = sendRequestToApiServer(countRequest);
        CountResponse cResponse = unmarshalResponse(response, CountResponse.class);
        return cResponse.getCount();
    }

    @Override
    public void clearMails(String recipient) {
        String normalizedEmail = EmailUtils.normalizeEmailAddress(recipient);
        ClearRequest clearRequest = new ClearRequest().setRecipient(normalizedEmail);

        String response = sendRequestToApiServer(clearRequest);
        StatusResponse sResponse = unmarshalResponse(response, StatusResponse.class);
        assert (sResponse.getStatus() == 200);
    }

    @Override
    public List<MailMessage> getMessages(String recipient) {
        String normalizedEmail = EmailUtils.normalizeEmailAddress(recipient);
        GetRequest getRequest = new GetRequest().setRecipient(normalizedEmail);

        String response;
        response = sendRequestToApiServer(getRequest);

        GetResponse getResponse = unmarshalResponse(response, GetResponse.class);
        return getResponse.getMessages();
    }

    @Override
    public List<MailMessage> getMessagesBySubject(String recipient, String subject) {
        logger.debug("Getting emails matching subject " + subject + " for email address " + recipient);
        List<MailMessage> allMessages = getMessages(recipient);
        List<MailMessage> matchingMessages = Lists.newArrayList();
        for (MailMessage message : allMessages) {
            logger.debug("Found email with subject " + message.getSubject());
            if (message.getSubject().contains(subject)) {
                matchingMessages.add(message);
            }
        }
        return matchingMessages;
    }

    @Override
    public boolean isRelayRecipient(String recipient) {
        String normalizedEmail = EmailUtils.normalizeEmailAddress(recipient);
        RelayRequest relayRequest = new RelayRequest(RelayMode.GET);
        relayRequest.setRecipient(normalizedEmail);
        String response = sendRequestToApiServer(relayRequest);
        StatusResponse statusResponse = unmarshalResponse(response, StatusResponse.class);
        return (statusResponse.getStatus() == 200);
    }

    @Override
    public void addRelayRecipient(String recipient) {
        String normalizedEmail = EmailUtils.normalizeEmailAddress(recipient);
        RelayRequest relayRequest = new RelayRequest(RelayMode.ADD);
        relayRequest.setRecipient(normalizedEmail);
        String response = sendRequestToApiServer(relayRequest);
        StatusResponse statusResponse = unmarshalResponse(response, StatusResponse.class);
        assert (statusResponse.getStatus() == 200);
    }

    @Override
    public void removeRelayRecipient(String recipient) {
        String normalizedEmail = EmailUtils.normalizeEmailAddress(recipient);
        RelayRequest relayRequest = new RelayRequest(RelayMode.REMOVE);
        relayRequest.setRecipient(normalizedEmail);
        String response = sendRequestToApiServer(relayRequest);
        StatusResponse statusResponse = unmarshalResponse(response, StatusResponse.class);
        assert (statusResponse.getStatus() == 200);
    }

    @Override
    public void clearRelayRecipients() {
        RelayRequest relayRequest = new RelayRequest(RelayMode.REMOVE);
        String response = sendRequestToApiServer(relayRequest);
        StatusResponse statusResponse = unmarshalResponse(response, StatusResponse.class);
        assert (statusResponse.getStatus() == 200);
    }

    @Override
    public List<String> getRelayRecipients() {
        RelayRequest relayRequest = new RelayRequest(RelayMode.GET);
        String response = sendRequestToApiServer(relayRequest);
        RelayResponse relayResponse = unmarshalResponse(response, RelayResponse.class);
        return relayResponse.getRelayRecipients().getRecipients();
    }

    @SuppressWarnings("unchecked")
    private <T> T unmarshalResponse(String response, Class<T> clazz) {
        try {
            return (T) ApiResponse.unmarshalResponse(response, clazz);
        } catch (Exception ioe) {
            throw new ApiException(ioe);
        }
    }

    private String sendRequestToApiServer(ApiRequest request) {
        try {
            Socket clientSocket = new Socket(this.apiServerHostname, this.apiServerPort);
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.writeBytes(request.toRequestString() + "\n");
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            clientSocket.close();
            return response.toString();
        } catch (ConnectException e) {
            throw new ApiException("SMTP mock not available at " + apiServerHostname + ":" + apiServerPort);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }
}
