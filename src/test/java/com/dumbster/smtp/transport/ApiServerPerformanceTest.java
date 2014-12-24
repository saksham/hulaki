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

package com.dumbster.smtp.transport;


import com.dumbster.smtp.api.ApiClient;
import com.dumbster.smtp.utils.TestInfrastructure;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@Test(groups = "Performance")
public class ApiServerPerformanceTest {
    private TestInfrastructure testInfrastructure = new TestInfrastructure();

    @Test(threadPoolSize = 50, invocationCount = 50)
    public void handles50ConcurrentApiConnections() {
        ApiClient client = new ApiClient(TestInfrastructure.API_HOSTNAME, TestInfrastructure.API_PORT);
        String recipient = RandomStringUtils.randomAlphabetic(15) + "@email.com";

        client.getMessages(recipient);

        verify(testInfrastructure.getMailMessageDao()).retrieveMessages(eq(recipient));
    }

    @BeforeClass
    public void startInfrastructure() throws Exception {
        testInfrastructure.startApiServer();
    }

    @AfterClass
    public void teardownInfrastructure() throws Exception {
        testInfrastructure.stop();
    }
}
