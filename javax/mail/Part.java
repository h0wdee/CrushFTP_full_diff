/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 */
package javax.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;

public interface Part {
    public static final String ATTACHMENT = "attachment";
    public static final String INLINE = "inline";

    public int getSize() throws MessagingException;

    public int getLineCount() throws MessagingException;

    public String getContentType() throws MessagingException;

    public boolean isMimeType(String var1) throws MessagingException;

    public String getDisposition() throws MessagingException;

    public void setDisposition(String var1) throws MessagingException;

    public String getDescription() throws MessagingException;

    public void setDescription(String var1) throws MessagingException;

    public String getFileName() throws MessagingException;

    public void setFileName(String var1) throws MessagingException;

    public InputStream getInputStream() throws IOException, MessagingException;

    public DataHandler getDataHandler() throws MessagingException;

    public Object getContent() throws IOException, MessagingException;

    public void setDataHandler(DataHandler var1) throws MessagingException;

    public void setContent(Object var1, String var2) throws MessagingException;

    public void setText(String var1) throws MessagingException;

    public void setContent(Multipart var1) throws MessagingException;

    public void writeTo(OutputStream var1) throws IOException, MessagingException;

    public String[] getHeader(String var1) throws MessagingException;

    public void setHeader(String var1, String var2) throws MessagingException;

    public void addHeader(String var1, String var2) throws MessagingException;

    public void removeHeader(String var1) throws MessagingException;

    public Enumeration getAllHeaders() throws MessagingException;

    public Enumeration getMatchingHeaders(String[] var1) throws MessagingException;

    public Enumeration getNonMatchingHeaders(String[] var1) throws MessagingException;
}

