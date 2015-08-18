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

package com.hulaki.smtp.transport;

import com.beust.jcommander.internal.Lists;
import com.hulaki.smtp.utils.TestGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test(groups = TestGroups.UNIT)
public class SmtpRequestTest {

    private static final SmtpCommand[] statelessCommands = {SmtpCommand.RSET, SmtpCommand.VRFY, SmtpCommand.EXPN, SmtpCommand.HELP, SmtpCommand.NOOP};
    private static final SmtpState[] commandStates = {SmtpState.CONNECT, SmtpState.GREET, SmtpState.MAIL, SmtpState.RCPT};
    private static final SmtpState[] dataStates = {SmtpState.DATA_HEADER, SmtpState.DATA_BODY};

    @DataProvider
    private Iterator<Object[]> commandStates() {
        List<Object[]> data = Lists.newArrayList();
        for (SmtpState state : commandStates) {
            data.add(new Object[]{state});
        }
        return data.iterator();
    }

    @Test(dataProvider = "commandStates")
    public void unrecognizedCommandIsIdentifiedOnAllCommandStates(SmtpState currentState) {
        SmtpRequest request = new SmtpRequest(currentState, SmtpCommand.parse("incorrect command"));

        SmtpResult smtpResult = request.execute();

        assertEquals(smtpResult.getNextState(), currentState);
        assertEquals(smtpResult.getSmtpResponse().getResponseCode(), 500);
        assertEquals(smtpResult.getSmtpResponse().getMessage(), "Command not recognized");
    }

    @DataProvider
    private Iterator<Object[]> dataStates() {
        List<Object[]> data = Lists.newArrayList();
        for(SmtpState state : dataStates) {
            data.add(new Object[]{state});
        }
        return data.iterator();
    }

    @Test(dataProvider = "dataStates")
    public void unrecognizedCommandProducesNoResponseOnDataStates(SmtpState currentState) {
        SmtpRequest request = new SmtpRequest(currentState, SmtpCommand.parse("some invalid command, but valid data"));

        SmtpResult smtpResult = request.execute();

        assertEquals(smtpResult.getNextState(), currentState);
        assertNull(smtpResult.getSmtpResponse());
    }

    @DataProvider
    private Object[][] validTransitions() {
        return new Object[][]{
                {SmtpState.CONNECT, SmtpCommand.CONNECT, SmtpState.GREET},
                {SmtpState.GREET, SmtpCommand.HELO, SmtpState.MAIL},
                {SmtpState.GREET, SmtpCommand.EHLO, SmtpState.MAIL},
                {SmtpState.MAIL, SmtpCommand.MAIL, SmtpState.RCPT},
                {SmtpState.RCPT, SmtpCommand.DATA, SmtpState.DATA_HEADER},
                {SmtpState.RCPT, SmtpCommand.RCPT, SmtpState.RCPT},
                {SmtpState.DATA_HEADER, SmtpCommand.BLANK_LINE, SmtpState.DATA_BODY},
                {SmtpState.DATA_HEADER, SmtpCommand.DATA_END, SmtpState.QUIT},
        };
    }

    @Test(dataProvider = "validTransitions")
    public void validTransitionsAreSupported(SmtpState currentState, SmtpCommand command, SmtpState newState) {
        SmtpRequest request = new SmtpRequest(currentState, command);

        SmtpResult smtpResult = request.execute();

        assertEquals(smtpResult.getNextState(), newState);
    }

    @DataProvider
    private Iterator<Object[]> statelessCommandsWithAllStates() {
        List<Object[]> data = Lists.newArrayList();
        for (SmtpState state : commandStates) {
            for (SmtpCommand command : statelessCommands) {
                data.add(new Object[]{state, command});
            }
        }
        return data.iterator();
    }

    @Test(dataProvider = "statelessCommandsWithAllStates")
    public void statelessCommandDoNotChangeStatesExceptRset(SmtpState currentState, SmtpCommand command) {
        SmtpRequest request = new SmtpRequest(currentState, command);

        SmtpResult smtpResult = request.execute();

        if(command == SmtpCommand.RSET) {
            assertEquals(smtpResult.getNextState(), SmtpState.GREET);
        } else {
            assertEquals(smtpResult.getNextState(), currentState);
        }
    }
}
