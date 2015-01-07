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

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "get-response", namespace = "https://github.com/saksham/hulaki")
public class GetResponse extends ApiResponse {

    private String recipient;

    @XmlElement(name = "mail-messages", type = MailMessages.class)
    private MailMessages mailMessages = new MailMessages();

    public GetResponse() { } // Empty constructor for JAXB

    public GetResponse(String recipient, List<MailMessage> smtpMessages) {
        this.recipient = recipient;
        this.mailMessages = new MailMessages(smtpMessages);
    }

    public String getRecipient() {
        return this.recipient;
    }

    public List<MailMessage> getMessages() {
        return this.mailMessages.getMessages();
    }
}
