/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
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
package com.dumbster.smtp.transport.old;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.mail.internet.MimeUtility;

/**
 * Container for a complete SMTP message - headers and message body.
 */
public class SmtpMessage {
    
    /** General Logger for this Class. */
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
        .getLog(SmtpMessage.class);
    
  /** Headers: Map of List of String hashed on header name. */
  private Map headers;
  /** Message body. */
  private StringBuffer body;
  /** the last Header-Name which is added with {@link #store(SmtpResponse, String)}. */
  private String lastHeaderName;
  /** flag if this SmtpObject is closed for Modifications. */
  private boolean closed;

  /**
   * Constructor. Initializes headers Map and body buffer.
   */
  public SmtpMessage() {
    headers = new HashMap(10);
    body = new StringBuffer();
  }

    /**
     * Update the headers or body depending on the SmtpResponse object and line of input.
     * 
     * @param response SmtpResponse object
     * @param params remainder of input line after SMTP command has been removed
     */
    public void store(final SmtpResponse response, final String params) {
        if (closed) {
            throw new IllegalStateException("This SmtpMessage Object is closed for modification.");
        }
        if (params != null) {
            if (SmtpState.DATA_HDR.equals(response.getNextState())) {
                if (isContinuationLine(params)) {
                    appendLastHeaderValue(lastHeaderName, encodedText(params.substring(1)));
                } else {
                    int headerNameEnd = params.indexOf(':');
                    if (headerNameEnd >= 0) {
                        String name = params.substring(0, headerNameEnd).trim();
                        String value = params.substring(headerNameEnd + 1).trim();
                        addHeader(name, encodedText(value));
                        lastHeaderName = name;
                    }
                }
            } else if (SmtpState.DATA_BODY == response.getNextState()) {
                String line = params;
                String encoding = getHeaderValue("Content-Transfer-Encoding");
                String charset = getCharset();
                if (charset != null && encoding != null) {
                  try {
                    line = new String(decode(line.getBytes("US-ASCII"), encoding), charset);
                  } catch (Exception e) {
                    LOG.error("Error decoding String '" + line + "': " + e.getMessage(), e);
                  }
                }
                body.append(line);
                body.append("\n");
            }
        }
    }
    
    private static byte[] decode(byte[] b, String encoding) throws Exception {
        if (b.length == 0) {
            return b;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStream decodedIs = MimeUtility.decode(bais, encoding);
        byte[] tmp = new byte[b.length];
        int n = decodedIs.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
     }  

    /**
     * Get the charset specified in Content-Type header.
     * 
     * @return charset, null if none specified.
     */
    public String getCharset() {
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
    
    /**
     * Get the charset specified in Content-Type header.
     * 
     * @return charset, null if none specified.
     */
    public String getContentTransferEncoding() {
        String contentTransferEncoding = getHeaderValue("Content-Transfer-Encoding");
        if (contentTransferEncoding != null) {
            return contentTransferEncoding;
        }
        return null;
    }

    /**
     * Indicates whether the given string contains a header continuation as specified by RFC 822.
     * @param candidate
     * @return true if it is a continuation line
     */
    private boolean isContinuationLine(String candidate) {
        return candidate.charAt(0) == 0x20 // ASCII SPACE
            || candidate.charAt(0) == 0x09; // ASCII HTAB
    }

    /**
     * @param encodedText
     * @return
     * @throws UnsupportedEncodingException
     */
    private String encodedText(String encodedText) {
        try {
            return MimeUtility.decodeText(encodedText);
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Error decode text '" + encodedText
                + "'. The unencoded Text will be returned.", e);
            return encodedText;
        }
    }

  /**
   * Get the first values associated with a given header name.
   * @param name header name
   * @return first value associated with the header name
   */
  public String getHeaderValue(String name) {
    List values = (List)headers.get(name);
    if (values == null) {
      return null;
    } else {
      Iterator iterator = values.iterator();
      return (String)iterator.next();
    }
  }

  /**
   * Get the message body.
   * @return message body
   */
  public String getBody() {
    return body.toString();
  }

  /**
   * Close this SmtpMessage Object for Modification.
   */
  public void close() {
    body.deleteCharAt(body.length() - 1);
    this.closed = true;
  }
  
  /**
   * Adds a header to the Map.
   * @param name header name
   * @param value header value
   */
  private void appendLastHeaderValue(String name, String value) {
    List valueList = (List)headers.get(name);
    if (valueList == null) {
      throw new IllegalStateException("updateLastHeaderValue() cannot find  Header: " + name);
    }
    String lastValue = (String) valueList.get(valueList.size() - 1);
    valueList.remove(valueList.size() - 1);
    valueList.add(lastValue + value);
  }

  /**
   * Adds a header to the Map.
   * @param name header name
   * @param value header value
   */
  private void addHeader(String name, String value) {
    List valueList = (List)headers.get(name);
    if (valueList == null) {
      valueList = new ArrayList(1);
      headers.put(name, valueList);
    }
    valueList.add(value);
  }

  /**
   * String representation of the SmtpMessage.
   * @return a String
   */
  public String toString() {
    StringBuffer msg = new StringBuffer();
    for(Iterator i = headers.keySet().iterator(); i.hasNext();) {
      String name = (String)i.next();
      List values = (List)headers.get(name);
      for(Iterator j = values.iterator(); j.hasNext();) {
        String value = (String)j.next();
        msg.append(name);
        msg.append(": ");
        msg.append(value);
        msg.append('\n');
      }
    }
    msg.append('\n');
    msg.append(body);
    msg.append('\n');
    return msg.toString();
  }
}
