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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Test(groups = "Unit")
public class ApiResponseTest {

    @Test(dataProvider = "allApiResponses")
    public void marshalAndUnmarshalResponses(ApiResponse apiResponse) throws Exception {
        String responseAsString = apiResponse.marshalResponse();

        ApiResponse reconstructed = ApiResponse.unmarshalResponse(responseAsString, apiResponse.getClass());

        assertNotNull(reconstructed);
    }

    @DataProvider
    private Object[][] allApiResponses() {
        return new Object[][]{
                {new RelayResponse()},
                {new CountResponse()},
                {new GetResponse()},
                {new StatusResponse()}
        };
    }
}