package com.github.saksham.hulaki.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtils {
    public static String normalizeEmailAddress(String email) {
        Pattern pattern = Pattern.compile("<[^<]*@[^>]*>");
        Matcher matcher = pattern.matcher(email);

        if (matcher.find()) {
            return email.substring(matcher.start() + 1, matcher.end() - 1);
        } else {
            return email;
        }
    }
}
