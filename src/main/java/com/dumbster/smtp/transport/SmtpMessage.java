package com.dumbster.smtp.transport;

import com.dumbster.smtp.exceptions.SmtpException;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.Map;

public class SmtpMessage {
    private final Map<String, String> headers = Maps.newHashMap();
    private final StringBuffer body = new StringBuffer();
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

        String encoding = getHeaderValue("Content-Transfer-Encoding");
        String charset = getCharset();
        if (charset != null && encoding != null) {
            try {
                line = decodeFromAscii(line, encoding, charset);
            } catch (Exception ex) {
                throw new SmtpException("Error decoding String '" + line + "': " + ex.getMessage(), ex);
            }
            body.append(line);
            body.append("\n");
        }
    }


    public void close() {
        this.closed = true;
        body.deleteCharAt(body.length() - 1);
    }


    public String getHeaderValue(String headerName) {
        if(headers.containsKey(headerName)) {
            return headers.get(headerName);
        }
        return null;
    }


    private void addHeader(String line) {
        int headerNameEnd = line.indexOf(':');
        if(headerNameEnd >= 0) {
            String name = line.substring(0, headerNameEnd).trim();
            String value = line.substring(headerNameEnd + 1).trim();
            headers.put(name, value);
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

    private static String decodeFromAscii(String line, String encoding, String charset) throws Exception {
        if (line.length() == 0) {
            return line;
        }

        byte[] asciiBytes = line.getBytes("US-ASCII");
        InputStream decodedStream = MimeUtility.decode(new ByteArrayInputStream(asciiBytes), encoding);
        byte[] tmp = new byte[asciiBytes.length];
        int n = decodedStream.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return new String(res, charset);
    }

    public String getBody() {
        return body.toString();
    }
}
