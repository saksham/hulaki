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

public abstract class ApiRequest {
    private String requestBody;
    private ApiCommand command;

    public ApiRequest(String requestBody, ApiCommand command) throws ApiProtocolException {
        this.requestBody = requestBody;
        this.command = command;
    }

    public ApiRequest(ApiCommand command) {
        this.command = command;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public ApiCommand getCommand() {
        return command;
    }

    public abstract String toRequestString();

    public static ApiRequest fromRequestString(String requestBody) {
        if (requestBody == null) {
            throw new ApiProtocolException("Unrecognized command received.");
        }

        String[] tokens = requestBody.split(" ");
        String command = tokens[0];
        if (command.equalsIgnoreCase(ApiCommand.COUNT.getCommand())) {
            return new CountRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.GET.getCommand())) {
            return new GetRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.CLEAR.getCommand())) {
            return new ClearRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.RELAY.getCommand())) {
            return new RelayRequest(requestBody);
        } else if (command.equalsIgnoreCase(ApiCommand.SERVER_STATUS.getCommand())) {
            return new ServerStatusRequest(requestBody);
        } else {
            return new InvalidRequest(requestBody);
        }
    }
}
