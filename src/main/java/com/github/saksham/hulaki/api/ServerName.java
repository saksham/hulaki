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

package com.github.saksham.hulaki.api;

import com.github.saksham.hulaki.exceptions.ApiProtocolException;
import org.apache.commons.lang3.StringUtils;

public enum ServerName {
    MAIL_PROCESSOR,
    SMTP_SERVER;


    public static ServerName parse(String nameAsString) {
        for(ServerName name : values()) {
            if (name.toString().equalsIgnoreCase(nameAsString)) {
                return name;
            }
        }
        throw new ApiProtocolException("Could not parse the server-name: " + nameAsString +
                ". Valid values are: " + StringUtils.join(values(), ','));
    }
}
