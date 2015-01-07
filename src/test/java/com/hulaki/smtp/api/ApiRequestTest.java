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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test(groups = "Unit")
public class ApiRequestTest {

    @Test(dataProvider = "allApiRequests")
    public void marshalAndUnmarshalRequests(ApiRequest apiRequest) throws Exception {
        String requestString = apiRequest.toRequestString();

        ApiRequest reconstructed = ApiRequest.fromRequestString(requestString);

        assertEquals(reconstructed.getRequestBody(), requestString);
        assertEquals(reconstructed.getCommand(), apiRequest.getCommand());
    }

    @DataProvider
    private Object[][] allApiRequests() {
        return new Object[][]{
                {new ClearRequest()},
                {new InvalidRequest()},
                {new ServerStatusRequest(ServerName.MAIL_PROCESSOR)},
                {new GetRequest()},
                {new CountRequest()},
                {new RelayRequest(RelayMode.GET)}
        };
    }
}