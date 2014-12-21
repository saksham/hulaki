package com.dumbster.smtp.transport;


import org.apache.commons.lang3.StringUtils;

public enum SmtpCommand {
    CONNECT("Connect"),
    EHLO("EHLO"),
    HELO("HELO"),
    MAIL("MAIL"),
    RCPT("RCPT"),
    DATA("DATA"),
    DATA_END("."),
    QUIT("QUIT"),

    RSET("RSET"),
    VRFY("VRFY"),
    EXPN("EXPN"),
    HELP("HELP"),
    NOOP("NOOP"),
    BLANK_LINE("Blank line"),

    UNRECOGNIZED("Unrecognized command / data");

    SmtpCommand(String command) {
        this.command = command;
    }

    private final String command;

    public static SmtpCommand parse(String line) {
        if(StringUtils.isEmpty(line)) {
            return BLANK_LINE;
        }

        if(line.equals(DATA_END.command)) {
            return DATA_END;
        }

        if(line.length() >= 4) {
            String firstFourLetters = line.substring(0, 4);
            for (SmtpCommand command : SmtpCommand.values()) {
                if (firstFourLetters.equalsIgnoreCase(command.command)) {
                    return command;
                }
            }
        }
        return UNRECOGNIZED;
    }
}
