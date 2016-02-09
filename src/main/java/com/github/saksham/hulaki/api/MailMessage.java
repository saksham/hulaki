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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mail-message", namespace = "https://github.com/saksham/hulaki")
public class MailMessage implements Serializable {
    private String to;
    private String from;
    private String subject;
    private String body;
    private boolean isRelayed;

    public MailMessage() {
    } // Empty constructor for JAXB

    public MailMessage(String from, String to, String subject, String body, boolean isRelayed) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.isRelayed = isRelayed;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isRelayed() {
        return isRelayed;
    }

    @Override
    public String toString() {
        return "To: " + to + ", Subject: " + subject;
    }
}
