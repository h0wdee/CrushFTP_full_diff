/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.ActivationDataFlavor
 *  javax.activation.DataSource
 */
package com.sun.mail.handlers;

import com.sun.mail.handlers.handler_base;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;

public class text_plain
extends handler_base {
    private static ActivationDataFlavor[] myDF = new ActivationDataFlavor[]{new ActivationDataFlavor(String.class, "text/plain", "Text String")};

    protected ActivationDataFlavor[] getDataFlavors() {
        return myDF;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object getContent(DataSource ds) throws IOException {
        String string;
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
            string = new String(buf, 0, pos);
            Object var10_11 = null;
        }
        catch (Throwable throwable) {
            Object var10_12 = null;
            try {
                is.close();
            }
            catch (IOException iOException) {}
            throw throwable;
        }
        try {
            is.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return string;
    }

    public void writeTo(Object obj, String type, OutputStream os) throws IOException {
        if (!(obj instanceof String)) {
            throw new IOException("\"" + this.getDataFlavors()[0].getMimeType() + "\" DataContentHandler requires String object, was given object of type " + obj.getClass().toString());
        }
        String enc = null;
        OutputStreamWriter osw = null;
        try {
            enc = this.getCharset(type);
            osw = new OutputStreamWriter((OutputStream)new NoCloseOutputStream(os), enc);
        }
        catch (IllegalArgumentException iex) {
            throw new UnsupportedEncodingException(enc);
        }
        String s = (String)obj;
        osw.write(s, 0, s.length());
        osw.close();
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

    private static class NoCloseOutputStream
    extends FilterOutputStream {
        public NoCloseOutputStream(OutputStream os) {
            super(os);
        }

        public void close() {
        }
    }
}

