package com.dumbster.smtp.transport;

import com.dumbster.smtp.exceptions.ApiException;
import org.testng.annotations.Test;

@Test(groups = "Component")
public class BindTest {

    public static final int PORT = 6869;

    @Test(expectedExceptions = {ApiException.class} )
    public void detectAndThrowExceptionForAddressAlreadyInUse() throws Exception {
        ApiServer firstServer = new ApiServer(PORT);
        ApiServer secondServer = new ApiServer(PORT);

        firstServer.startAndWait();
        secondServer.startAndWait();
    }
}
