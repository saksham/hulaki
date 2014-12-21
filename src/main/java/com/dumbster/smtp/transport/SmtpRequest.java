package com.dumbster.smtp.transport;


import com.dumbster.smtp.exceptions.SmtpProtocolException;

import java.net.InetAddress;

/**
 * Contains an SMTP client request. Handles state transitions using the following state transition table.
 * <PRE>
 * -----------+-------------------------------------------------------------------------------------------------
 *            |                                 State
 *  Action    +-------------+-----------+-----------+--------------+---------------+---------------+------------
 *            | CONNECT     | GREET     | MAIL      | RCPT         | DATA_HDR      | DATA_BODY     | QUIT
 * -----------+-------------+-----------+-----------+--------------+---------------+---------------+------------
 * connect    | 220/GREET   | 503/GREET | 503/MAIL  | 503/RCPT     | 503/DATA_HDR  | 503/DATA_BODY | 503/QUIT
 * ehlo       | 503/CONNECT | 250/MAIL  | 503/MAIL  | 503/RCPT     | 503/DATA_HDR  | 503/DATA_BODY | 503/QUIT
 * mail       | 503/CONNECT | 503/GREET | 250/RCPT  | 503/RCPT     | 503/DATA_HDR  | 503/DATA_BODY | 250/RCPT
 * rcpt       | 503/CONNECT | 503/GREET | 503/MAIL  | 250/RCPT     | 503/DATA_HDR  | 503/DATA_BODY | 503/QUIT
 * data       | 503/CONNECT | 503/GREET | 503/MAIL  | 354/DATA_HDR | 503/DATA_HDR  | 503/DATA_BODY | 503/QUIT
 * data_end   | 503/CONNECT | 503/GREET | 503/MAIL  | 503/RCPT     | 250/QUIT      | 250/QUIT      | 503/QUIT
 * unrecog    | 500/CONNECT | 500/GREET | 500/MAIL  | 500/RCPT     | ---/DATA_HDR  | ---/DATA_BODY | 500/QUIT
 * quit       | 503/CONNECT | 503/GREET | 503/MAIL  | 503/RCPT     | 503/DATA_HDR  | 503/DATA_BODY | 250/CONNECT
 * blank_line | 503/CONNECT | 503/GREET | 503/MAIL  | 503/RCPT     | ---/DATA_BODY | ---/DATA_BODY | 503/QUIT
 * rset       | 250/GREET   | 250/GREET | 250/GREET | 250/GREET    | 250/GREET     | 250/GREET     | 250/GREET
 * vrfy       | 252/CONNECT | 252/GREET | 252/MAIL  | 252/RCPT     | 252/DATA_HDR  | 252/DATA_BODY | 252/QUIT
 * expn       | 252/CONNECT | 252/GREET | 252/MAIL  | 252/RCPT     | 252/DATA_HDR  | 252/DATA_BODY | 252/QUIT
 * help       | 211/CONNECT | 211/GREET | 211/MAIL  | 211/RCPT     | 211/DATA_HDR  | 211/DATA_BODY | 211/QUIT
 * noop       | 250/CONNECT | 250/GREET | 250/MAIL  | 250/RCPT     | 250|DATA_HDR  | 250/DATA_BODY | 250/QUIT
 * </PRE>
 */

public class SmtpRequest {
    private final SmtpState currentState;
    private final SmtpCommand command;
    private static String hostname;

    static {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            hostname = "?";
        }
    }

    public SmtpRequest(SmtpState currentState, SmtpCommand command) {
        this.command = command;
        this.currentState = currentState;
    }

    public SmtpResult execute() {
        switch(command) {
            case VRFY:
            case EXPN:
                return new SmtpResult(currentState, 252, "Not supported");
            case HELP:
                return new SmtpResult(currentState, 211, "No help available");
            case NOOP:
                return new SmtpResult(currentState, 250, "OK");
            case RSET:
                return new SmtpResult(SmtpState.GREET, 250, "OK");
            case CONNECT:
                return toStatefulResponse(new SmtpResult(SmtpState.GREET, 220, "ESMTP " + hostname + " service ready"), SmtpState.CONNECT);
            case EHLO:
            case HELO:
                return toStatefulResponse(new SmtpResult(SmtpState.MAIL, 250, "OK"), SmtpState.GREET);
            case MAIL:
                return toStatefulResponse(new SmtpResult(SmtpState.RCPT, 250, "OK"), SmtpState.MAIL, SmtpState.QUIT);
            case RCPT:
                return toStatefulResponse(new SmtpResult(SmtpState.RCPT, 250, "OK"), SmtpState.RCPT);
            case DATA:
                return toStatefulResponse(new SmtpResult(SmtpState.DATA_HEADER, 354, "Start mail input; end with <CRLF>.<CRLF>"), SmtpState.RCPT);
            case UNRECOGNIZED:
                if(currentState == SmtpState.DATA_HEADER || currentState == SmtpState.DATA_BODY) {
                    return toStatefulResponse(new SmtpResult(currentState), SmtpState.DATA_HEADER, SmtpState.DATA_BODY);
                } else {
                    return new SmtpResult(currentState, 500, "Command not recognized");
                }
            case DATA_END:
                return toStatefulResponse(new SmtpResult(SmtpState.QUIT, 250, "OK"), SmtpState.DATA_HEADER, SmtpState.DATA_BODY);
            case BLANK_LINE:
                if(currentState == SmtpState.DATA_HEADER) {
                    return toStatefulResponse(new SmtpResult(SmtpState.DATA_BODY), SmtpState.DATA_HEADER);
                } else {
                    return toStatefulResponse(new SmtpResult(currentState), SmtpState.DATA_BODY);
                }
            case QUIT:
                return toStatefulResponse(new SmtpResult(SmtpState.QUIT, 221, hostname + " closing transmission channel"), SmtpState.CONNECT);
            default:
                throw new SmtpProtocolException("Unable to process request");
        }
    }

    private SmtpResult toStatefulResponse(SmtpResult correctResponse, SmtpState... allowedCurrentStates) {
        for(SmtpState allowedCurrentState : allowedCurrentStates) {
            if(currentState == allowedCurrentState) {
                return correctResponse;
            }
        }
        return new SmtpResult(currentState, 503, "Bad sequence of commands: "+ command.name());
    }
}
