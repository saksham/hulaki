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

import com.dumbster.smtp.transport.ApiServer;
import org.mockito.InjectMocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(groups = "Component")
public class ApiClientTest {
    private static final int PORT = 6869;
    private IApiClient apiClient = new ApiClient("localhost", PORT);

    @InjectMocks
    private ApiServer apiServer = new ApiServer(PORT);

    @Test
    public void countMailsSucceeds() throws Exception {
        int count = apiClient.countMails();

        assertEquals(count, 0);
    }

    @BeforeClass
    private void startApiServer() throws Exception {
        apiServer.start();
    }

}
