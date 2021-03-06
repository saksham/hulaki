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

import java.util.List;

public interface IApiClient {
    void waitForNewEmailOrFail(String recipient, int oldEmailCount) throws InterruptedException;

    void waitForNewEmailOrFail(String recipient, int oldEmailCount, long timeoutInMillis) throws InterruptedException;

    int countMails();

    int countMails(String recipient);

    void clearMails(String recipient);

    List<MailMessage> getMessages(String recipient);

    boolean isRelayRecipient(String recipient);

    void addRelayRecipient(String recipient);

    void removeRelayRecipient(String recipient);

    void clearRelayRecipients();

    List<String> getRelayRecipients();
}
