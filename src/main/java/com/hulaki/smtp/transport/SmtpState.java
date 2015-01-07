package com.hulaki.smtp.transport;


public enum SmtpState {
    CONNECT,
    GREET,
    MAIL,
    RCPT,
    DATA_HEADER,
    DATA_BODY,
    QUIT,
    DISCONNECT
}
