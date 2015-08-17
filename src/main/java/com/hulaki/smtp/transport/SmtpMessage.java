package com.hulaki.smtp.transport;

import com.hulaki.smtp.exceptions.SmtpException;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmtpMessage {
    private static final Pattern headerPattern = Pattern.compile("(?<name>[^:]*): (?<value>.*)");
    private static final Logger logger = LogManager.getLogger(SmtpServer.class);
    
    private Map<String, String> headers = Maps.newHashMap();
    private StringBuffer bodyBuffer = new StringBuffer();
    private String body;
    
    private String latestHeader;
    private boolean isReadingHeader = true;
    private boolean closed = false;

    public void addLine(String line) {
        if(closed) {
            throw new SmtpException("Can't add content once the message is closed.");
        }

        if (isReadingHeader && StringUtils.isEmpty(line)) {
            isReadingHeader = false;
            return;
        }

        if (isReadingHeader) {
            addHeader(line);
            return;
        }
        
        bodyBuffer.append(line).append("\n");
        String encoding = getEncoding();
        if(encoding != null) {
            boolean isSoftBreak = encoding.equalsIgnoreCase("quoted-printable") && line.endsWith("=3D");
            if(isSoftBreak) {
                bodyBuffer.delete(bodyBuffer.length() - 4, bodyBuffer.length() - 1);
            }
        }
    }


    public void close() {
        this.closed = true;
        if(bodyBuffer.length() > 0) {
            bodyBuffer.deleteCharAt(bodyBuffer.length() - 1);
        }

        String encoding = getEncoding();
        String charset = getCharset();
        if (charset != null && encoding != null) {
            try {
                body = decodeFromUtf8(bodyBuffer.toString(), encoding, charset);
            } catch (Exception ex) {
                throw new SmtpException("Error decoding body '" + bodyBuffer + "': " + ex.getMessage(), ex);
            }
        } else {
            body = bodyBuffer.toString();
        }
        
    }

    private String getEncoding() {
        return getHeaderValue("Content-Transfer-Encoding");
    }


    private String encodedText(String encodedText) {
        try {
            return MimeUtility.decodeText(encodedText);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Error decode text '" + encodedText
                    + "'. The unencoded Text will be returned.", e);
            return encodedText;
        }
    }

    public String getHeaderValue(String headerName) {
        if(headers.containsKey(headerName)) {
            return headers.get(headerName);
        }
        return null;
    }


    private void addHeader(String line) {
        Matcher headerMatcher = headerPattern.matcher(line);
        if (headerMatcher.matches()) {
            String name = headerMatcher.group("name");
            latestHeader = name;
            String value = headerMatcher.group("value");
            headers.put(name, encodedText(value));
        } else {
            String value = headers.get(latestHeader) + encodedText(line.substring(1));
            headers.put(latestHeader, value);
        }
    }


    private String getCharset() {
        String contentType = getHeaderValue("Content-Type");
        if (contentType != null) {
            int n = contentType.indexOf("charset=");
            if (n >= 0) {
                String charset = contentType.substring(n + "charset=".length());
                n = charset.indexOf(";");
                if (n >= 0) {
                    charset = charset.substring(0, n);
                }
                return charset;
            }
        }
        return null;
    }

    private static String decodeFromUtf8(String text, String encoding, String charset) throws Exception {
        if (text.length() == 0) {
            return text;
        }

        byte[] asciiBytes = text.getBytes("UTF-8");
        InputStream decodedStream = MimeUtility.decode(new ByteArrayInputStream(asciiBytes), encoding);
        byte[] tmp = new byte[asciiBytes.length];
        int n = decodedStream.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return new String(res, charset);
    }

    public String getBody() {
        return body;
    }
}
