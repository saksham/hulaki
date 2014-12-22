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
import org.apache.commons.lang3.StringUtils;

public class ServerStatusRequest extends ApiRequest {

    private final ServerName serverName;

    public ServerStatusRequest(ServerName serverName) {
        super("", ApiCommand.SERVER_STATUS);
        this.serverName = serverName;
    }
    
    public ServerStatusRequest(String requestBody) throws ApiProtocolException {
        super(requestBody, ApiCommand.SERVER_STATUS);
        String[] tokens = requestBody.split(" ");
        if(tokens.length != 2) {
            throw new ApiProtocolException("Server status should be in format SERVER_STATUS [" + StringUtils.join(ServerName.values(), '|')+"]");
        }
        serverName = ServerName.parse(tokens[1].trim());
    }


    @Override
    public String toRequestString() {
        return ApiCommand.SERVER_STATUS.toString() + " " + serverName ;
    }

    public ServerName getServerName() {
        return serverName;
    }
}
