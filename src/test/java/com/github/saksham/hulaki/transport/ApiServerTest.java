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

package com.github.saksham.hulaki.transport;

import com.github.saksham.hulaki.utils.TestGroups;
import com.github.saksham.hulaki.utils.TestInfrastructure;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.COMPONENT)
public class ApiServerTest {
    private TestInfrastructure infrastructure = new TestInfrastructure();

    @BeforeMethod
    private void startApiServer() throws Exception {
        infrastructure.getApiServer().start();
    }

    @AfterClass
    private void teardownInfrastructure() throws Exception {
        infrastructure.stop();
    }
}