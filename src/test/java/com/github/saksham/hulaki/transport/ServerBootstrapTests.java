package com.github.saksham.hulaki.transport;

import com.github.saksham.hulaki.utils.TestGroups;
import com.github.saksham.hulaki.utils.TestInfrastructure;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = TestGroups.UNIT)
public class ServerBootstrapTests {

    private TestInfrastructure testInfrastructure = new TestInfrastructure();

    @DataProvider
    private Object[][] serverDataProvider() {
        return new Object[][]{
                {testInfrastructure.getApiServer()},
                {testInfrastructure.getSmtpServer()}
        };
    }
    
    @Test(dataProvider = "serverDataProvider")
    public void restartServerMultipleTimes(Server server) throws Exception {
        int numOfRestarts = 5;

        for (int i = 0; i < numOfRestarts; i++) {
            server.start();
            assertTrue(server.isRunning());
            server.stop();
            assertFalse(server.isRunning());
        }
    }

    @Test(dataProvider = "serverDataProvider")
    public void stoppedServerCanBeStartedMultipleTimes(Server server) throws Exception {
        final int numOfStarts = 10;
        server.stop();

        for (int i = 0; i < numOfStarts; i++) {
            server.start();
            assertTrue(server.isRunning());
        }
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test(dataProvider = "serverDataProvider")
    public void startedSeverCanBeStoppedMultipleTimes(Server server) throws Exception {
        final int numOfStops = 10;
        server.start();

        for (int i = 0; i < numOfStops; i++) {
            server.stop();
        }
        assertFalse(server.isRunning());
    }
}
