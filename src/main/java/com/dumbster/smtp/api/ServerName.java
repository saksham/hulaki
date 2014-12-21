package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiProtocolException;
import org.apache.commons.lang3.StringUtils;

public enum ServerName {
    MAIL_PROCESSOR,
    SMTP_SERVER;


    public static ServerName parse(String nameAsString) {
        for(ServerName name : values()) {
            if (name.toString().equalsIgnoreCase(nameAsString)) {
                return name;
            }
        }
        throw new ApiProtocolException("Could not parse the server-name: " + nameAsString +
                ". Valid values are: " + StringUtils.join(values(), ','));
    }
}
