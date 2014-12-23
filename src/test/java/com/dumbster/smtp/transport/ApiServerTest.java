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

import com.dumbster.smtp.utils.TestInfrastructure;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "Component")
public class ApiServerTest {
    private TestInfrastructure infrastructure;

    @BeforeMethod
    private void createNewServer() {
        infrastructure = new TestInfrastructure();
        infrastructure.inject();
    }
    
    @AfterMethod
    private void stopServer() throws Exception {
        infrastructure.stop();
    }
    
    @Test(enabled = false)
    public void restartApiServerMultipleTimes() throws Exception {
        int numOfRestarts = 10;

        for (int i = 0; i < numOfRestarts; i++) {
            infrastructure.getApiServer().start();
            assertTrue(infrastructure.getApiServer().isRunning());
            infrastructure.stop();
            assertFalse(infrastructure.getApiServer().isRunning());
        }
    }

    @Test
    public void serverCanBeStartedMultipleTimes() throws Exception {
        int numOfStarts = 10;

        for (int i = 0; i < numOfStarts; i++) {
            infrastructure.getApiServer().start();
            assertTrue(infrastructure.getApiServer().isRunning());
        }
        infrastructure.getApiServer().stop();
        assertFalse(infrastructure.getApiServer().isRunning());
    }

    @Test
    public void serverCanBeStoppedMultipleTimes() throws Exception {
        int numOfStops = 10;

        infrastructure.getApiServer().start();
        for (int i = 0; i < numOfStops; i++) {
            infrastructure.getApiServer().stop();
        }
        assertFalse(infrastructure.getApiServer().isRunning());
    }
}