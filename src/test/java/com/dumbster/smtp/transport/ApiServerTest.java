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

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "Component")
public class ApiServerTest {
    private ApiServer apiServer = new ApiServer(6869);

    public void restartApiServerMultipleTimes() throws Exception {
        int numOfRestarts = 2;

        for (int i = 0; i < numOfRestarts; i++) {
            apiServer.start();
            assertFalse(apiServer.isStopped());
            apiServer.stop();
            assertTrue(apiServer.isStopped());
        }
    }

    public void serverCantBeStartedMultipleTimes() throws Exception {
        int numOfStarts = 10;

        for (int i = 0; i < numOfStarts; i++) {
            apiServer.start();
            assertFalse(apiServer.isStopped());
        }
        apiServer.stop();
        assertTrue(apiServer.isStopped());
    }

    public void serverCanBeStoppedMultipleTimes() throws Exception {
        int numOfStops = 10;

        apiServer.start();
        for (int i = 0; i < numOfStops; i++) {
            apiServer.stop();
        }
        assertTrue(apiServer.isStopped());
    }


}