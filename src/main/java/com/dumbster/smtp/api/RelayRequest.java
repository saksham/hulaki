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


import com.dumbster.smtp.exceptions.ApiProtocolException;
import com.dumbster.smtp.utils.EmailUtils;
import org.apache.commons.lang3.StringUtils;

public class RelayRequest extends ApiRequest {

    private final RelayMode relayMode;
    private String recipient;

    public RelayRequest(RelayMode relayMode) {
        super(ApiCommand.RELAY);
        this.relayMode = relayMode;
    }

    public RelayRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.RELAY);
        String[] tokens = requestBody.split(" ");
        if (tokens.length == 2) {
            this.relayMode = RelayMode.parse(tokens[1]);
        } else if (tokens.length == 3) {
            this.relayMode = RelayMode.parse(tokens[1]);
            this.recipient = EmailUtils.normalizeEmailAddress(tokens[2]);
        } else {
            throw new ApiProtocolException("Invalid format. Should be in format: " + ApiCommand.RELAY.toString() + " [" +
                    StringUtils.join(RelayMode.all(), "|") + "] [email-address]");
        }
    }


    public String getRecipient() {
        return recipient;
    }

    public RelayRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public RelayMode getRelayMode() {
        return relayMode;
    }


    @Override
    public String toRequestString() {
        StringBuilder request = new StringBuilder(ApiCommand.RELAY.toString());
        request.append(" ").append(this.relayMode);
        if(this.recipient != null) {
            request.append(" ").append(recipient);
        }
        return request.toString();
    }
}
