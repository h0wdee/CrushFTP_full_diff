/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.ActivationDataFlavor
 *  javax.activation.DataContentHandler
 *  javax.activation.DataSource
 */
package com.sun.mail.dsn;

import com.sun.mail.dsn.MessageHeaders;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;

public class text_rfc822headers
implements DataContentHandler {
    private static ActivationDataFlavor myDF = new ActivationDataFlavor(MessageHeaders.class, "text/rfc822-headers", "RFC822 headers");
    private static ActivationDataFlavor myDFs = new ActivationDataFlavor(String.class, "text/rfc822-headers", "RFC822 headers");

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{myDF, myDFs};
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws IOException {
        if (myDF.equals(df)) {
            return this.getContent(ds);
        }
        if (myDFs.equals(df)) {
            return this.getStringContent(ds);
        }
        return null;
    }

    public Object getContent(DataSource ds) throws IOException {
        try {
            return new MessageHeaders(ds.getInputStream());
        }
        catch (MessagingException mex) {
            throw new IOException("Exception creating MessageHeaders: " + mex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Object getStringContent(DataSource ds) throws IOException {
        String enc = null;
        InputStreamReader is = null;
        try {
            enc = this.getCharset(ds.getContentType());
            is = new InputStreamReader(ds.getInputStream(), enc);
        }
        catch (IllegalArgumentException iex) {
            throw new UnsupportedEncodingException(enc);
        }
        try {
            int count;
            int pos = 0;
            char[] buf = new char[1024];
            while ((count = is.read(buf, pos, buf.length - pos)) != -1) {
                if ((pos += count) < buf.length) continue;
                int size = buf.length;
                size = size < 262144 ? (size += size) : (size += 262144);
                char[] tbuf = new char[size];
                System.arraycopy(buf, 0, tbuf, 0, pos);
                buf = tbuf;
            }
            String string = new String(buf, 0, pos);
            return string;
        }
        finally {
            try {
                is.close();
            }
            catch (IOException iOException) {}
        }
    }

    public void writeTo(Object obj, String type, OutputStream os) throws IOException {
        if (obj instanceof MessageHeaders) {
            MessageHeaders mh = (MessageHeaders)obj;
            try {
                mh.writeTo(os);
            }
            catch (MessagingException mex) {
                Exception ex = mex.getNextException();
                if (ex instanceof IOException) {
                    throw (IOException)ex;
                }
                throw new IOException("Exception writing headers: " + mex);
            }
            return;
        }
        if (!(obj instanceof String)) {
            throw new IOException("\"" + myDFs.getMimeType() + "\" DataContentHandler requires String object, was given object of type " + obj.getClass().toString());
        }
        String enc = null;
        OutputStreamWriter osw = null;
        try {
            enc = this.getCharset(type);
            osw = new OutputStreamWriter(os, enc);
        }
        catch (IllegalArgumentException iex) {
            throw new UnsupportedEncodingException(enc);
        }
        String s = (String)obj;
        osw.write(s, 0, s.length());
        osw.flush();
    }

    private String getCharset(String type) {
        try {
            ContentType ct = new ContentType(type);
            String charset = ct.getParameter("charset");
            if (charset == null) {
                charset = "us-ascii";
            }
            return MimeUtility.javaCharset(charset);
        }
        catch (Exception ex) {
            return null;
        }
    }
}

