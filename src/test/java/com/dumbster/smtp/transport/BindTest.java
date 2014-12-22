package com.dumbster.smtp.transport;

import org.testng.annotations.Test;

import java.net.BindException;

@Test(groups = "Component")
public class BindTest {

    public static final int PORT = 6869;

    @Test(expectedExceptions = {BindException.class})
    public void detectAndThrowExceptionForAddressAlreadyInUse() throws Exception {
        ApiServer firstServer = new ApiServer(PORT);
        ApiServer secondServer = new ApiServer(PORT);

        firstServer.start();
        secondServer.start();
    }
}
