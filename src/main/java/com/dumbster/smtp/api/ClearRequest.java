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

public class ClearRequest extends ApiRequest {

    private String recipient;

    public ClearRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.CLEAR);
        String[] tokens = requestBody.split(" ");
        if (tokens.length != 2) {
            throw new ApiProtocolException("The count request should be in format COUNT <email-address>");
        }
        this.recipient = tokens[1];
    }

    public ClearRequest() {
        super(ApiCommand.CLEAR);
    }

    public String getRecipient() {
        return recipient;
    }

    public ClearRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String toRequestString() {
        return getCommand() + " " + getRecipient();
    }
}
