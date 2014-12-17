package com.dumbster.smtp.utils;

import com.dumbster.smtp.transport.Observer;
import com.dumbster.smtp.transport.SmtpMessage;
import com.google.common.collect.Lists;

import java.util.List;

/**
* Created by saksham.gautam on 17.12.2014.
*/
public class SimpleSmtpStorage implements Observer<SmtpMessage> {
    private List<SmtpMessage> smtpMessages = Lists.newArrayList();


    @Override
    public void notify(SmtpMessage data) {
        smtpMessages.add(data);
    }

    public SmtpMessage getLatestEmail() {
        if(smtpMessages.size() == 0) {
            return null;
        } else {
            return smtpMessages.get(smtpMessages.size() - 1);
        }
    }

    public int getReceivedEmailSize() {
        return smtpMessages.size();
    }
}
