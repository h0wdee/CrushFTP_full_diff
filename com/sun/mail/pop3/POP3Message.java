/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.pop3;

import com.sun.mail.pop3.AppendStream;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.pop3.TempFile;
import com.sun.mail.util.ReadableMime;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.IllegalWriteException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.SharedInputStream;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class POP3Message
extends MimeMessage
implements ReadableMime {
    static final String UNKNOWN = "UNKNOWN";
    private POP3Folder folder;
    private int hdrSize = -1;
    private int msgSize = -1;
    String uid = "UNKNOWN";
    private SoftReference<InputStream> rawData = new SoftReference<Object>(null);

    public POP3Message(Folder folder, int msgno) throws MessagingException {
        super(folder, msgno);
        assert (folder instanceof POP3Folder);
        this.folder = (POP3Folder)folder;
    }

    @Override
    public synchronized void setFlags(Flags newFlags, boolean set) throws MessagingException {
        Flags oldFlags = (Flags)this.flags.clone();
        super.setFlags(newFlags, set);
        if (!this.flags.equals(oldFlags)) {
            this.folder.notifyMessageChangedListeners(1, this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getSize() throws MessagingException {
        try {
            POP3Message pOP3Message = this;
            synchronized (pOP3Message) {
                if (this.msgSize > 0) {
                    return this.msgSize;
                }
            }
            if (this.headers == null) {
                this.loadHeaders();
            }
            pOP3Message = this;
            synchronized (pOP3Message) {
                if (this.msgSize < 0) {
                    this.msgSize = this.folder.getProtocol().list(this.msgnum) - this.hdrSize;
                }
                return this.msgSize;
            }
        }
        catch (EOFException eex) {
            this.folder.close(false);
            throw new FolderClosedException(this.folder, eex.toString());
        }
        catch (IOException ex) {
            throw new MessagingException("error getting size", ex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private InputStream getRawStream(boolean skipHeader) throws MessagingException {
        InputStream rawcontent = null;
        try {
            POP3Message pOP3Message = this;
            synchronized (pOP3Message) {
                rawcontent = this.rawData.get();
                if (rawcontent == null) {
                    TempFile cache = this.folder.getFileCache();
                    if (cache != null) {
                        if (this.folder.logger.isLoggable(Level.FINE)) {
                            this.folder.logger.fine("caching message #" + this.msgnum + " in temp file");
                        }
                        AppendStream os = cache.getAppendStream();
                        BufferedOutputStream bos = new BufferedOutputStream(os);
                        try {
                            this.folder.getProtocol().retr(this.msgnum, bos);
                            Object var8_11 = null;
                        }
                        catch (Throwable throwable) {
                            Object var8_12 = null;
                            bos.close();
                            throw throwable;
                        }
                        bos.close();
                        rawcontent = os.getInputStream();
                    } else {
                        rawcontent = this.folder.getProtocol().retr(this.msgnum, this.msgSize > 0 ? this.msgSize + this.hdrSize : 0);
                    }
                    if (rawcontent == null) {
                        this.expunged = true;
                        throw new MessageRemovedException("can't retrieve message #" + this.msgnum + " in POP3Message.getContentStream");
                    }
                    if (this.headers == null || ((POP3Store)this.folder.getStore()).forgetTopHeaders) {
                        this.headers = new InternetHeaders(rawcontent);
                        this.hdrSize = (int)((SharedInputStream)((Object)rawcontent)).getPosition();
                    } else {
                        int len;
                        boolean offset = false;
                        block8: do {
                            int c1;
                            len = 0;
                            while ((c1 = rawcontent.read()) >= 0 && c1 != 10) {
                                if (c1 == 13) {
                                    if (rawcontent.available() <= 0) continue block8;
                                    rawcontent.mark(1);
                                    if (rawcontent.read() == 10) continue block8;
                                    rawcontent.reset();
                                    continue block8;
                                }
                                ++len;
                            }
                        } while (rawcontent.available() != 0 && len != 0);
                        this.hdrSize = (int)((SharedInputStream)((Object)rawcontent)).getPosition();
                    }
                    this.msgSize = rawcontent.available();
                    this.rawData = new SoftReference<InputStream>(rawcontent);
                }
            }
        }
        catch (EOFException eex) {
            this.folder.close(false);
            throw new FolderClosedException(this.folder, eex.toString());
        }
        catch (IOException ex) {
            throw new MessagingException("error fetching POP3 content", ex);
        }
        rawcontent = ((SharedInputStream)((Object)rawcontent)).newStream(skipHeader ? (long)this.hdrSize : 0L, -1L);
        return rawcontent;
    }

    @Override
    protected synchronized InputStream getContentStream() throws MessagingException {
        if (this.contentStream != null) {
            return ((SharedInputStream)((Object)this.contentStream)).newStream(0L, -1L);
        }
        InputStream cstream = this.getRawStream(true);
        TempFile cache = this.folder.getFileCache();
        if (cache != null || ((POP3Store)this.folder.getStore()).keepMessageContent) {
            this.contentStream = ((SharedInputStream)((Object)cstream)).newStream(0L, -1L);
        }
        return cstream;
    }

    @Override
    public InputStream getMimeStream() throws MessagingException {
        return this.getRawStream(false);
    }

    public synchronized void invalidate(boolean invalidateHeaders) {
        this.content = null;
        InputStream rstream = this.rawData.get();
        if (rstream != null) {
            try {
                rstream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            this.rawData = new SoftReference<Object>(null);
        }
        if (this.contentStream != null) {
            try {
                this.contentStream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            this.contentStream = null;
        }
        this.msgSize = -1;
        if (invalidateHeaders) {
            this.headers = null;
            this.hdrSize = -1;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public InputStream top(int n) throws MessagingException {
        try {
            POP3Message pOP3Message = this;
            synchronized (pOP3Message) {
                return this.folder.getProtocol().top(this.msgnum, n);
            }
        }
        catch (EOFException eex) {
            this.folder.close(false);
            throw new FolderClosedException(this.folder, eex.toString());
        }
        catch (IOException ex) {
            throw new MessagingException("error getting size", ex);
        }
    }

    @Override
    public String[] getHeader(String name) throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getHeader(name);
    }

    @Override
    public String getHeader(String name, String delimiter) throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getHeader(name, delimiter);
    }

    @Override
    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    @Override
    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    @Override
    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    @Override
    public Enumeration<Header> getAllHeaders() throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getAllHeaders();
    }

    @Override
    public Enumeration<Header> getMatchingHeaders(String[] names) throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getMatchingHeaders(names);
    }

    @Override
    public Enumeration<Header> getNonMatchingHeaders(String[] names) throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getNonMatchingHeaders(names);
    }

    @Override
    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    @Override
    public Enumeration<String> getAllHeaderLines() throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getAllHeaderLines();
    }

    @Override
    public Enumeration<String> getMatchingHeaderLines(String[] names) throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getMatchingHeaderLines(names);
    }

    @Override
    public Enumeration<String> getNonMatchingHeaderLines(String[] names) throws MessagingException {
        if (this.headers == null) {
            this.loadHeaders();
        }
        return this.headers.getNonMatchingHeaderLines(names);
    }

    @Override
    public void saveChanges() throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void writeTo(OutputStream os, String[] ignoreList) throws IOException, MessagingException {
        block10: {
            block12: {
                InputStream rawcontent;
                block11: {
                    rawcontent = this.rawData.get();
                    if (rawcontent != null || ignoreList != null || ((POP3Store)this.folder.getStore()).cacheWriteTo) break block11;
                    if (this.folder.logger.isLoggable(Level.FINE)) {
                        this.folder.logger.fine("streaming msg " + this.msgnum);
                    }
                    if (!this.folder.getProtocol().retr(this.msgnum, os)) {
                        this.expunged = true;
                        throw new MessageRemovedException("can't retrieve message #" + this.msgnum + " in POP3Message.writeTo");
                    }
                    break block10;
                }
                if (rawcontent == null || ignoreList != null) break block12;
                InputStream in = ((SharedInputStream)((Object)rawcontent)).newStream(0L, -1L);
                try {
                    int len;
                    byte[] buf = new byte[16384];
                    while ((len = in.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                    Object var8_7 = null;
                }
                catch (Throwable throwable) {
                    Object var8_8 = null;
                    try {
                        if (in != null) {
                            in.close();
                        }
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    throw throwable;
                }
                try {
                    if (in != null) {
                        in.close();
                    }
                    break block10;
                }
                catch (IOException iOException) {}
                break block10;
            }
            super.writeTo(os, ignoreList);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void loadHeaders() throws MessagingException {
        assert (!Thread.holdsLock(this));
        try {
            boolean fetchContent = false;
            POP3Message pOP3Message = this;
            synchronized (pOP3Message) {
                if (this.headers != null) {
                    return;
                }
                InputStream hdrs = null;
                if (((POP3Store)this.folder.getStore()).disableTop || (hdrs = this.folder.getProtocol().top(this.msgnum, 0)) == null) {
                    fetchContent = true;
                } else {
                    try {
                        this.hdrSize = hdrs.available();
                        this.headers = new InternetHeaders(hdrs);
                        Object var5_6 = null;
                    }
                    catch (Throwable throwable) {
                        Object var5_7 = null;
                        hdrs.close();
                        throw throwable;
                    }
                    hdrs.close();
                    {
                    }
                }
            }
            if (!fetchContent) return;
            InputStream cs = null;
            try {
                cs = this.getContentStream();
                Object var8_10 = null;
                if (cs == null) return;
            }
            catch (Throwable throwable) {
                Object var8_11 = null;
                if (cs == null) throw throwable;
                cs.close();
                throw throwable;
            }
            cs.close();
            return;
        }
        catch (EOFException eex) {
            this.folder.close(false);
            throw new FolderClosedException(this.folder, eex.toString());
        }
        catch (IOException ex) {
            throw new MessagingException("error loading POP3 headers", ex);
        }
    }
}

