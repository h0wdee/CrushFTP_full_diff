/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.internet;

import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.Part;

public interface MimePart
extends Part {
    public String getHeader(String var1, String var2) throws MessagingException;

    public void addHeaderLine(String var1) throws MessagingException;

    public Enumeration getAllHeaderLines() throws MessagingException;

    public Enumeration getMatchingHeaderLines(String[] var1) throws MessagingException;

    public Enumeration getNonMatchingHeaderLines(String[] var1) throws MessagingException;

    public String getEncoding() throws MessagingException;

    public String getContentID() throws MessagingException;

    public String getContentMD5() throws MessagingException;

    public void setContentMD5(String var1) throws MessagingException;

    public String[] getContentLanguage() throws MessagingException;

    public void setContentLanguage(String[] var1) throws MessagingException;

    public void setText(String var1) throws MessagingException;

    public void setText(String var1, String var2) throws MessagingException;

    public void setText(String var1, String var2, String var3) throws MessagingException;
}

