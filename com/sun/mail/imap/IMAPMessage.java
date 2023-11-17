/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 *  javax.activation.DataSource
 */
package com.sun.mail.imap;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPInputStream;
import com.sun.mail.imap.IMAPMultipartDataSource;
import com.sun.mail.imap.IMAPNestedMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Utility;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FetchItem;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.MODSEQ;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.RFC822SIZE;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.ReadableMime;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class IMAPMessage
extends MimeMessage
implements ReadableMime {
    protected BODYSTRUCTURE bs;
    protected ENVELOPE envelope;
    protected Map<String, Object> items;
    private Date receivedDate;
    private int size = -1;
    private Boolean peek;
    private volatile long uid = -1L;
    private volatile long modseq = -1L;
    protected String sectionId;
    private String type;
    private String subject;
    private String description;
    private volatile boolean headersLoaded = false;
    private volatile boolean bodyLoaded = false;
    private Hashtable<String, String> loadedHeaders = new Hashtable(1);
    static final String EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";

    protected IMAPMessage(IMAPFolder folder, int msgnum) {
        super(folder, msgnum);
        this.flags = null;
    }

    protected IMAPMessage(Session session) {
        super(session);
    }

    protected IMAPProtocol getProtocol() throws ProtocolException, FolderClosedException {
        ((IMAPFolder)this.folder).waitIfIdle();
        IMAPProtocol p = ((IMAPFolder)this.folder).protocol;
        if (p == null) {
            throw new FolderClosedException(this.folder);
        }
        return p;
    }

    protected boolean isREV1() throws FolderClosedException {
        IMAPProtocol p = ((IMAPFolder)this.folder).protocol;
        if (p == null) {
            throw new FolderClosedException(this.folder);
        }
        return p.isREV1();
    }

    protected Object getMessageCacheLock() {
        return ((IMAPFolder)this.folder).messageCacheLock;
    }

    protected int getSequenceNumber() {
        return ((IMAPFolder)this.folder).messageCache.seqnumOf(this.getMessageNumber());
    }

    @Override
    protected void setMessageNumber(int msgnum) {
        super.setMessageNumber(msgnum);
    }

    protected long getUID() {
        return this.uid;
    }

    protected void setUID(long uid) {
        this.uid = uid;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized long getModSeq() throws MessagingException {
        if (this.modseq != -1L) {
            return this.modseq;
        }
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                MODSEQ ms = p.fetchMODSEQ(this.getSequenceNumber());
                if (ms != null) {
                    this.modseq = ms.modseq;
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        return this.modseq;
    }

    long _getModSeq() {
        return this.modseq;
    }

    void setModSeq(long modseq) {
        this.modseq = modseq;
    }

    @Override
    protected void setExpunged(boolean set) {
        super.setExpunged(set);
    }

    protected void checkExpunged() throws MessageRemovedException {
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void forceCheckExpunged() throws MessageRemovedException, FolderClosedException {
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                this.getProtocol().noop();
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException protocolException) {
                // empty catch block
            }
        }
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    protected int getFetchBlockSize() {
        return ((IMAPStore)this.folder.getStore()).getFetchBlockSize();
    }

    protected boolean ignoreBodyStructureSize() {
        return ((IMAPStore)this.folder.getStore()).ignoreBodyStructureSize();
    }

    @Override
    public Address[] getFrom() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getFrom();
        }
        this.loadEnvelope();
        InternetAddress[] a = this.envelope.from;
        if (a == null || a.length == 0) {
            a = this.envelope.sender;
        }
        return this.aaclone(a);
    }

    @Override
    public void setFrom(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public void addFrom(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public Address getSender() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getSender();
        }
        this.loadEnvelope();
        if (this.envelope.sender != null && this.envelope.sender.length > 0) {
            return this.envelope.sender[0];
        }
        return null;
    }

    @Override
    public void setSender(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public Address[] getRecipients(Message.RecipientType type) throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getRecipients(type);
        }
        this.loadEnvelope();
        if (type == Message.RecipientType.TO) {
            return this.aaclone(this.envelope.to);
        }
        if (type == Message.RecipientType.CC) {
            return this.aaclone(this.envelope.cc);
        }
        if (type == Message.RecipientType.BCC) {
            return this.aaclone(this.envelope.bcc);
        }
        return super.getRecipients(type);
    }

    @Override
    public void setRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public void addRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public Address[] getReplyTo() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getReplyTo();
        }
        this.loadEnvelope();
        if (this.envelope.replyTo == null || this.envelope.replyTo.length == 0) {
            return this.getFrom();
        }
        return this.aaclone(this.envelope.replyTo);
    }

    @Override
    public void setReplyTo(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public String getSubject() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getSubject();
        }
        if (this.subject != null) {
            return this.subject;
        }
        this.loadEnvelope();
        if (this.envelope.subject == null) {
            return null;
        }
        try {
            this.subject = MimeUtility.decodeText(MimeUtility.unfold(this.envelope.subject));
        }
        catch (UnsupportedEncodingException ex) {
            this.subject = this.envelope.subject;
        }
        return this.subject;
    }

    @Override
    public void setSubject(String subject, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public Date getSentDate() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getSentDate();
        }
        this.loadEnvelope();
        if (this.envelope.date == null) {
            return null;
        }
        return new Date(this.envelope.date.getTime());
    }

    @Override
    public void setSentDate(Date d) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        this.checkExpunged();
        if (this.receivedDate == null) {
            this.loadEnvelope();
        }
        if (this.receivedDate == null) {
            return null;
        }
        return new Date(this.receivedDate.getTime());
    }

    @Override
    public int getSize() throws MessagingException {
        this.checkExpunged();
        if (this.size == -1) {
            this.loadEnvelope();
        }
        return this.size;
    }

    @Override
    public int getLineCount() throws MessagingException {
        this.checkExpunged();
        this.loadBODYSTRUCTURE();
        return this.bs.lines;
    }

    @Override
    public String[] getContentLanguage() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getContentLanguage();
        }
        this.loadBODYSTRUCTURE();
        if (this.bs.language != null) {
            return (String[])this.bs.language.clone();
        }
        return null;
    }

    @Override
    public void setContentLanguage(String[] languages) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getInReplyTo() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getHeader("In-Reply-To", " ");
        }
        this.loadEnvelope();
        return this.envelope.inReplyTo;
    }

    @Override
    public synchronized String getContentType() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getContentType();
        }
        if (this.type == null) {
            this.loadBODYSTRUCTURE();
            ContentType ct = new ContentType(this.bs.type, this.bs.subtype, this.bs.cParams);
            this.type = ct.toString();
        }
        return this.type;
    }

    @Override
    public String getDisposition() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getDisposition();
        }
        this.loadBODYSTRUCTURE();
        return this.bs.disposition;
    }

    @Override
    public void setDisposition(String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public String getEncoding() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getEncoding();
        }
        this.loadBODYSTRUCTURE();
        return this.bs.encoding;
    }

    @Override
    public String getContentID() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getContentID();
        }
        this.loadBODYSTRUCTURE();
        return this.bs.id;
    }

    @Override
    public void setContentID(String cid) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public String getContentMD5() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getContentMD5();
        }
        this.loadBODYSTRUCTURE();
        return this.bs.md5;
    }

    @Override
    public void setContentMD5(String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public String getDescription() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getDescription();
        }
        if (this.description != null) {
            return this.description;
        }
        this.loadBODYSTRUCTURE();
        if (this.bs.description == null) {
            return null;
        }
        try {
            this.description = MimeUtility.decodeText(this.bs.description);
        }
        catch (UnsupportedEncodingException ex) {
            this.description = this.bs.description;
        }
        return this.description;
    }

    @Override
    public void setDescription(String description, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public String getMessageID() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getMessageID();
        }
        this.loadEnvelope();
        return this.envelope.messageId;
    }

    @Override
    public String getFileName() throws MessagingException {
        this.checkExpunged();
        if (this.bodyLoaded) {
            return super.getFileName();
        }
        String filename = null;
        this.loadBODYSTRUCTURE();
        if (this.bs.dParams != null) {
            filename = this.bs.dParams.get("filename");
        }
        if (filename == null && this.bs.cParams != null) {
            filename = this.bs.cParams.get("name");
        }
        return filename;
    }

    @Override
    public void setFileName(String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected InputStream getContentStream() throws MessagingException {
        if (this.bodyLoaded) {
            return super.getContentStream();
        }
        ByteArrayInputStream is = null;
        boolean pk = this.getPeek();
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                if (p.isREV1() && this.getFetchBlockSize() != -1) {
                    return new IMAPInputStream(this, this.toSection("TEXT"), this.bs != null && !this.ignoreBodyStructureSize() ? this.bs.size : -1, pk);
                }
                if (p.isREV1()) {
                    BODY b = pk ? p.peekBody(this.getSequenceNumber(), this.toSection("TEXT")) : p.fetchBody(this.getSequenceNumber(), this.toSection("TEXT"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), "TEXT");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            throw new MessagingException("No content");
        }
        return is;
    }

    @Override
    public synchronized DataHandler getDataHandler() throws MessagingException {
        this.checkExpunged();
        if (this.dh == null && !this.bodyLoaded) {
            this.loadBODYSTRUCTURE();
            if (this.type == null) {
                ContentType ct = new ContentType(this.bs.type, this.bs.subtype, this.bs.cParams);
                this.type = ct.toString();
            }
            if (this.bs.isMulti()) {
                this.dh = new DataHandler((DataSource)new IMAPMultipartDataSource(this, this.bs.bodies, this.sectionId, this));
            } else if (this.bs.isNested() && this.isREV1() && this.bs.envelope != null) {
                this.dh = new DataHandler((Object)new IMAPNestedMessage(this, this.bs.bodies[0], this.bs.envelope, this.sectionId == null ? "1" : this.sectionId + ".1"), this.type);
            }
        }
        return super.getDataHandler();
    }

    @Override
    public void setDataHandler(DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public InputStream getMimeStream() throws MessagingException {
        ByteArrayInputStream is = null;
        boolean pk = this.getPeek();
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                if (p.isREV1() && this.getFetchBlockSize() != -1) {
                    return new IMAPInputStream(this, this.sectionId, -1, pk);
                }
                if (p.isREV1()) {
                    BODY b = pk ? p.peekBody(this.getSequenceNumber(), this.sectionId) : p.fetchBody(this.getSequenceNumber(), this.sectionId);
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), null);
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            this.forceCheckExpunged();
            throw new MessagingException("No content");
        }
        return is;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeTo(OutputStream os) throws IOException, MessagingException {
        if (this.bodyLoaded) {
            super.writeTo(os);
            return;
        }
        InputStream is = this.getMimeStream();
        try {
            int count;
            byte[] bytes = new byte[16384];
            while ((count = is.read(bytes)) != -1) {
                os.write(bytes, 0, count);
            }
            Object var6_5 = null;
        }
        catch (Throwable throwable) {
            Object var6_6 = null;
            is.close();
            throw throwable;
        }
        is.close();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String[] getHeader(String name) throws MessagingException {
        this.checkExpunged();
        if (this.isHeaderLoaded(name)) {
            return this.headers.getHeader(name);
        }
        ByteArrayInputStream is = null;
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                if (p.isREV1()) {
                    BODY b = p.peekBody(this.getSequenceNumber(), this.toSection("HEADER.FIELDS (" + name + ")"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), "HEADER.LINES (" + name + ")");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            return null;
        }
        if (this.headers == null) {
            this.headers = new InternetHeaders();
        }
        this.headers.load(is);
        this.setHeaderLoaded(name);
        return this.headers.getHeader(name);
    }

    @Override
    public String getHeader(String name, String delimiter) throws MessagingException {
        this.checkExpunged();
        if (this.getHeader(name) == null) {
            return null;
        }
        return this.headers.getHeader(name, delimiter);
    }

    @Override
    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public Enumeration<Header> getAllHeaders() throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getAllHeaders();
    }

    @Override
    public Enumeration<Header> getMatchingHeaders(String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getMatchingHeaders(names);
    }

    @Override
    public Enumeration<Header> getNonMatchingHeaders(String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getNonMatchingHeaders(names);
    }

    @Override
    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public Enumeration<String> getAllHeaderLines() throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getAllHeaderLines();
    }

    @Override
    public Enumeration<String> getMatchingHeaderLines(String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getMatchingHeaderLines(names);
    }

    @Override
    public Enumeration<String> getNonMatchingHeaderLines(String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }

    @Override
    public synchronized Flags getFlags() throws MessagingException {
        this.checkExpunged();
        this.loadFlags();
        return super.getFlags();
    }

    @Override
    public synchronized boolean isSet(Flags.Flag flag) throws MessagingException {
        this.checkExpunged();
        this.loadFlags();
        return super.isSet(flag);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                p.storeFlags(this.getSequenceNumber(), flag, set);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }

    public synchronized void setPeek(boolean peek) {
        this.peek = peek;
    }

    public synchronized boolean getPeek() {
        if (this.peek == null) {
            return ((IMAPStore)this.folder.getStore()).getPeek();
        }
        return this.peek;
    }

    public synchronized void invalidateHeaders() {
        this.headersLoaded = false;
        this.loadedHeaders.clear();
        this.headers = null;
        this.envelope = null;
        this.bs = null;
        this.receivedDate = null;
        this.size = -1;
        this.type = null;
        this.subject = null;
        this.description = null;
        this.flags = null;
        this.content = null;
        this.contentStream = null;
        this.bodyLoaded = false;
    }

    protected boolean handleFetchItem(Item item, String[] hdrs, boolean allHeaders) throws MessagingException {
        if (item instanceof Flags) {
            this.flags = (Flags)((Object)item);
        } else if (item instanceof ENVELOPE) {
            this.envelope = (ENVELOPE)item;
        } else if (item instanceof INTERNALDATE) {
            this.receivedDate = ((INTERNALDATE)item).getDate();
        } else if (item instanceof RFC822SIZE) {
            this.size = ((RFC822SIZE)item).size;
        } else if (item instanceof MODSEQ) {
            this.modseq = ((MODSEQ)item).modseq;
        } else if (item instanceof BODYSTRUCTURE) {
            this.bs = (BODYSTRUCTURE)item;
        } else if (item instanceof UID) {
            UID u = (UID)item;
            this.uid = u.uid;
            if (((IMAPFolder)this.folder).uidTable == null) {
                ((IMAPFolder)this.folder).uidTable = new Hashtable();
            }
            ((IMAPFolder)this.folder).uidTable.put(u.uid, this);
        } else if (item instanceof RFC822DATA || item instanceof BODY) {
            boolean isHeader;
            ByteArrayInputStream headerStream;
            if (item instanceof RFC822DATA) {
                headerStream = ((RFC822DATA)item).getByteArrayInputStream();
                isHeader = ((RFC822DATA)item).isHeader();
            } else {
                headerStream = ((BODY)item).getByteArrayInputStream();
                isHeader = ((BODY)item).isHeader();
            }
            if (!isHeader) {
                try {
                    this.size = ((InputStream)headerStream).available();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                this.parse(headerStream);
                this.bodyLoaded = true;
                this.setHeadersLoaded(true);
            } else {
                InternetHeaders h = new InternetHeaders();
                if (headerStream != null) {
                    h.load(headerStream);
                }
                if (this.headers == null || allHeaders) {
                    this.headers = h;
                } else {
                    Enumeration e = h.getAllHeaders();
                    while (e.hasMoreElements()) {
                        Header he = (Header)e.nextElement();
                        if (this.isHeaderLoaded(he.getName())) continue;
                        this.headers.addHeader(he.getName(), he.getValue());
                    }
                }
                if (allHeaders) {
                    this.setHeadersLoaded(true);
                } else {
                    for (int k = 0; k < hdrs.length; ++k) {
                        this.setHeaderLoaded(hdrs[k]);
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    protected void handleExtensionFetchItems(Map<String, Object> extensionItems) {
        if (extensionItems == null || extensionItems.isEmpty()) {
            return;
        }
        if (this.items == null) {
            this.items = new HashMap<String, Object>();
        }
        this.items.putAll(extensionItems);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Object fetchItem(FetchItem fitem) throws MessagingException {
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            Object robj = null;
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                int seqnum = this.getSequenceNumber();
                Response[] r = p.fetch(seqnum, fitem.getName());
                for (int i = 0; i < r.length; ++i) {
                    Object o;
                    if (r[i] == null || !(r[i] instanceof FetchResponse) || ((FetchResponse)r[i]).getNumber() != seqnum) continue;
                    FetchResponse f = (FetchResponse)r[i];
                    this.handleExtensionFetchItems(f.getExtensionItems());
                    if (this.items == null || (o = this.items.get(fitem.getName())) == null) continue;
                    robj = o;
                }
                p.notifyResponseHandlers(r);
                p.handleResult(r[r.length - 1]);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
            return robj;
        }
    }

    public synchronized Object getItem(FetchItem fitem) throws MessagingException {
        Object item;
        Object object = item = this.items == null ? null : this.items.get(fitem.getName());
        if (item == null) {
            item = this.fetchItem(fitem);
        }
        return item;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void loadEnvelope() throws MessagingException {
        if (this.envelope != null) {
            return;
        }
        Response[] r = null;
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                int seqnum = this.getSequenceNumber();
                r = p.fetch(seqnum, EnvelopeCmd);
                for (int i = 0; i < r.length; ++i) {
                    if (r[i] == null || !(r[i] instanceof FetchResponse) || ((FetchResponse)r[i]).getNumber() != seqnum) continue;
                    FetchResponse f = (FetchResponse)r[i];
                    int count = f.getItemCount();
                    for (int j = 0; j < count; ++j) {
                        Item item = f.getItem(j);
                        if (item instanceof ENVELOPE) {
                            this.envelope = (ENVELOPE)item;
                            continue;
                        }
                        if (item instanceof INTERNALDATE) {
                            this.receivedDate = ((INTERNALDATE)item).getDate();
                            continue;
                        }
                        if (!(item instanceof RFC822SIZE)) continue;
                        this.size = ((RFC822SIZE)item).size;
                    }
                }
                p.notifyResponseHandlers(r);
                p.handleResult(r[r.length - 1]);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (this.envelope == null) {
            throw new MessagingException("Failed to load IMAP envelope");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void loadBODYSTRUCTURE() throws MessagingException {
        if (this.bs != null) {
            return;
        }
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                this.bs = p.fetchBodyStructure(this.getSequenceNumber());
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
            if (this.bs == null) {
                this.forceCheckExpunged();
                throw new MessagingException("Unable to load BODYSTRUCTURE");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void loadHeaders() throws MessagingException {
        if (this.headersLoaded) {
            return;
        }
        ByteArrayInputStream is = null;
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                if (p.isREV1()) {
                    BODY b = p.peekBody(this.getSequenceNumber(), this.toSection("HEADER"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), "HEADER");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            throw new MessagingException("Cannot load header");
        }
        this.headers = new InternetHeaders(is);
        this.headersLoaded = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void loadFlags() throws MessagingException {
        if (this.flags != null) {
            return;
        }
        Object object = this.getMessageCacheLock();
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                this.checkExpunged();
                this.flags = p.fetchFlags(this.getSequenceNumber());
                if (this.flags == null) {
                    this.flags = new Flags();
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }

    private boolean areHeadersLoaded() {
        return this.headersLoaded;
    }

    private void setHeadersLoaded(boolean loaded) {
        this.headersLoaded = loaded;
    }

    private boolean isHeaderLoaded(String name) {
        if (this.headersLoaded) {
            return true;
        }
        return this.loadedHeaders.containsKey(name.toUpperCase(Locale.ENGLISH));
    }

    private void setHeaderLoaded(String name) {
        this.loadedHeaders.put(name.toUpperCase(Locale.ENGLISH), name);
    }

    private String toSection(String what) {
        if (this.sectionId == null) {
            return what;
        }
        return this.sectionId + "." + what;
    }

    private InternetAddress[] aaclone(InternetAddress[] aa) {
        if (aa == null) {
            return null;
        }
        return (InternetAddress[])aa.clone();
    }

    private Flags _getFlags() {
        return this.flags;
    }

    private ENVELOPE _getEnvelope() {
        return this.envelope;
    }

    private BODYSTRUCTURE _getBodyStructure() {
        return this.bs;
    }

    void _setFlags(Flags flags) {
        this.flags = flags;
    }

    Session _getSession() {
        return this.session;
    }

    public static class FetchProfileCondition
    implements Utility.Condition {
        private boolean needEnvelope = false;
        private boolean needFlags = false;
        private boolean needBodyStructure = false;
        private boolean needUID = false;
        private boolean needHeaders = false;
        private boolean needSize = false;
        private boolean needMessage = false;
        private boolean needRDate = false;
        private String[] hdrs = null;
        private Set<FetchItem> need = new HashSet<FetchItem>();

        public FetchProfileCondition(FetchProfile fp, FetchItem[] fitems) {
            if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                this.needEnvelope = true;
            }
            if (fp.contains(FetchProfile.Item.FLAGS)) {
                this.needFlags = true;
            }
            if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
                this.needBodyStructure = true;
            }
            if (fp.contains(FetchProfile.Item.SIZE)) {
                this.needSize = true;
            }
            if (fp.contains(UIDFolder.FetchProfileItem.UID)) {
                this.needUID = true;
            }
            if (fp.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
                this.needHeaders = true;
            }
            if (fp.contains(IMAPFolder.FetchProfileItem.SIZE)) {
                this.needSize = true;
            }
            if (fp.contains(IMAPFolder.FetchProfileItem.MESSAGE)) {
                this.needMessage = true;
            }
            if (fp.contains(IMAPFolder.FetchProfileItem.INTERNALDATE)) {
                this.needRDate = true;
            }
            this.hdrs = fp.getHeaderNames();
            for (int i = 0; i < fitems.length; ++i) {
                if (!fp.contains(fitems[i].getFetchProfileItem())) continue;
                this.need.add(fitems[i]);
            }
        }

        public boolean test(IMAPMessage m) {
            if (this.needEnvelope && m._getEnvelope() == null && !m.bodyLoaded) {
                return true;
            }
            if (this.needFlags && m._getFlags() == null) {
                return true;
            }
            if (this.needBodyStructure && m._getBodyStructure() == null && !m.bodyLoaded) {
                return true;
            }
            if (this.needUID && m.getUID() == -1L) {
                return true;
            }
            if (this.needHeaders && !m.areHeadersLoaded()) {
                return true;
            }
            if (this.needSize && m.size == -1 && !m.bodyLoaded) {
                return true;
            }
            if (this.needMessage && !m.bodyLoaded) {
                return true;
            }
            if (this.needRDate && m.receivedDate == null) {
                return true;
            }
            for (int i = 0; i < this.hdrs.length; ++i) {
                if (m.isHeaderLoaded(this.hdrs[i])) continue;
                return true;
            }
            for (FetchItem fitem : this.need) {
                if (m.items != null && m.items.get(fitem.getName()) != null) continue;
                return true;
            }
            return false;
        }
    }
}

