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

package com.hulaki.smtp.api;


import com.hulaki.smtp.exceptions.ApiProtocolException;
import com.hulaki.smtp.utils.EmailUtils;

public class CountRequest extends ApiRequest {

    private String recipient;

    public CountRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.COUNT);
        String[] tokens = requestBody.split(" ");
        if (tokens.length == 2) {
            this.recipient = EmailUtils.normalizeEmailAddress(tokens[1]);
        } else if (tokens.length > 2) {
            throw new ApiProtocolException("The count request should be in format COUNT [<email-address>]");
        }

    }

    public CountRequest() {
        super(ApiCommand.COUNT);
    }

    public String getRecipient() {
        return recipient;
    }

    public CountRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String toRequestString() {
        if(recipient != null) {
            return getCommand().toString() + " " + recipient;
        } else {
            return getCommand().toString();
        }
    }
}
