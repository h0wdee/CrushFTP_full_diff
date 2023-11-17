/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 */
package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.MessageRemovedIOException;
import com.sun.mail.util.MimeUtil;
import com.sun.mail.util.PropUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.NewsAddress;
import javax.mail.internet.SharedInputStream;
import javax.mail.internet.UniqueValue;
import javax.mail.util.SharedByteArrayInputStream;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MimeMessage
extends Message
implements MimePart {
    protected DataHandler dh;
    protected byte[] content;
    protected InputStream contentStream;
    protected InternetHeaders headers;
    protected Flags flags;
    protected boolean modified = false;
    protected boolean saved = false;
    protected Object cachedContent;
    private static final MailDateFormat mailDateFormat = new MailDateFormat();
    private boolean strict = true;
    private static final Flags answeredFlag = new Flags(Flags.Flag.ANSWERED);

    public MimeMessage(Session session) {
        super(session);
        this.modified = true;
        this.headers = new InternetHeaders();
        this.flags = new Flags();
        this.initStrict();
    }

    public MimeMessage(Session session, InputStream is) throws MessagingException {
        super(session);
        this.flags = new Flags();
        this.initStrict();
        this.parse(is);
        this.saved = true;
    }

    public MimeMessage(MimeMessage source) throws MessagingException {
        super(source.session);
        int size;
        this.flags = source.getFlags();
        if (this.flags == null) {
            this.flags = new Flags();
        }
        ByteArrayOutputStream bos = (size = source.getSize()) > 0 ? new ByteArrayOutputStream(size) : new ByteArrayOutputStream();
        try {
            this.strict = source.strict;
            source.writeTo(bos);
            bos.close();
            SharedByteArrayInputStream bis = new SharedByteArrayInputStream(bos.toByteArray());
            this.parse(bis);
            bis.close();
            this.saved = true;
        }
        catch (IOException ex) {
            throw new MessagingException("IOException while copying message", ex);
        }
    }

    protected MimeMessage(Folder folder, int msgnum) {
        super(folder, msgnum);
        this.flags = new Flags();
        this.saved = true;
        this.initStrict();
    }

    protected MimeMessage(Folder folder, InputStream is, int msgnum) throws MessagingException {
        this(folder, msgnum);
        this.initStrict();
        this.parse(is);
    }

    protected MimeMessage(Folder folder, InternetHeaders headers, byte[] content, int msgnum) throws MessagingException {
        this(folder, msgnum);
        this.headers = headers;
        this.content = content;
        this.initStrict();
    }

    private void initStrict() {
        if (this.session != null) {
            this.strict = PropUtil.getBooleanSessionProperty(this.session, "mail.mime.address.strict", true);
        }
    }

    protected void parse(InputStream is) throws MessagingException {
        if (!(is instanceof ByteArrayInputStream || is instanceof BufferedInputStream || is instanceof SharedInputStream)) {
            is = new BufferedInputStream(is);
        }
        this.headers = this.createInternetHeaders(is);
        if (is instanceof SharedInputStream) {
            SharedInputStream sis = (SharedInputStream)((Object)is);
            this.contentStream = sis.newStream(sis.getPosition(), -1L);
        } else {
            try {
                this.content = ASCIIUtility.getBytes(is);
            }
            catch (IOException ioex) {
                throw new MessagingException("IOException", ioex);
            }
        }
        this.modified = false;
    }

    @Override
    public Address[] getFrom() throws MessagingException {
        Address[] a = this.getAddressHeader("From");
        if (a == null) {
            a = this.getAddressHeader("Sender");
        }
        return a;
    }

    @Override
    public void setFrom(Address address) throws MessagingException {
        if (address == null) {
            this.removeHeader("From");
        } else {
            this.setHeader("From", MimeUtility.fold(6, address.toString()));
        }
    }

    public void setFrom(String address) throws MessagingException {
        if (address == null) {
            this.removeHeader("From");
        } else {
            this.setAddressHeader("From", InternetAddress.parse(address));
        }
    }

    @Override
    public void setFrom() throws MessagingException {
        InternetAddress me = null;
        try {
            me = InternetAddress._getLocalAddress(this.session);
        }
        catch (Exception ex) {
            throw new MessagingException("No From address", ex);
        }
        if (me == null) {
            throw new MessagingException("No From address");
        }
        this.setFrom(me);
    }

    @Override
    public void addFrom(Address[] addresses) throws MessagingException {
        this.addAddressHeader("From", addresses);
    }

    public Address getSender() throws MessagingException {
        Address[] a = this.getAddressHeader("Sender");
        if (a == null || a.length == 0) {
            return null;
        }
        return a[0];
    }

    public void setSender(Address address) throws MessagingException {
        if (address == null) {
            this.removeHeader("Sender");
        } else {
            this.setHeader("Sender", MimeUtility.fold(8, address.toString()));
        }
    }

    @Override
    public Address[] getRecipients(Message.RecipientType type) throws MessagingException {
        if (type == RecipientType.NEWSGROUPS) {
            String s = this.getHeader("Newsgroups", ",");
            return s == null ? null : NewsAddress.parse(s);
        }
        return this.getAddressHeader(this.getHeaderName(type));
    }

    @Override
    public Address[] getAllRecipients() throws MessagingException {
        Address[] all = super.getAllRecipients();
        Address[] ng = this.getRecipients(RecipientType.NEWSGROUPS);
        if (ng == null) {
            return all;
        }
        if (all == null) {
            return ng;
        }
        Address[] addresses = new Address[all.length + ng.length];
        System.arraycopy(all, 0, addresses, 0, all.length);
        System.arraycopy(ng, 0, addresses, all.length, ng.length);
        return addresses;
    }

    @Override
    public void setRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
        if (type == RecipientType.NEWSGROUPS) {
            if (addresses == null || addresses.length == 0) {
                this.removeHeader("Newsgroups");
            } else {
                this.setHeader("Newsgroups", NewsAddress.toString(addresses));
            }
        } else {
            this.setAddressHeader(this.getHeaderName(type), addresses);
        }
    }

    public void setRecipients(Message.RecipientType type, String addresses) throws MessagingException {
        if (type == RecipientType.NEWSGROUPS) {
            if (addresses == null || addresses.length() == 0) {
                this.removeHeader("Newsgroups");
            } else {
                this.setHeader("Newsgroups", addresses);
            }
        } else {
            this.setAddressHeader(this.getHeaderName(type), addresses == null ? null : InternetAddress.parse(addresses));
        }
    }

    @Override
    public void addRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
        if (type == RecipientType.NEWSGROUPS) {
            String s = NewsAddress.toString(addresses);
            if (s != null) {
                this.addHeader("Newsgroups", s);
            }
        } else {
            this.addAddressHeader(this.getHeaderName(type), addresses);
        }
    }

    public void addRecipients(Message.RecipientType type, String addresses) throws MessagingException {
        if (type == RecipientType.NEWSGROUPS) {
            if (addresses != null && addresses.length() != 0) {
                this.addHeader("Newsgroups", addresses);
            }
        } else {
            this.addAddressHeader(this.getHeaderName(type), InternetAddress.parse(addresses));
        }
    }

    @Override
    public Address[] getReplyTo() throws MessagingException {
        Address[] a = this.getAddressHeader("Reply-To");
        if (a == null || a.length == 0) {
            a = this.getFrom();
        }
        return a;
    }

    @Override
    public void setReplyTo(Address[] addresses) throws MessagingException {
        this.setAddressHeader("Reply-To", addresses);
    }

    private Address[] getAddressHeader(String name) throws MessagingException {
        String s = this.getHeader(name, ",");
        return s == null ? null : InternetAddress.parseHeader(s, this.strict);
    }

    private void setAddressHeader(String name, Address[] addresses) throws MessagingException {
        String s = InternetAddress.toString(addresses, name.length() + 2);
        if (s == null) {
            this.removeHeader(name);
        } else {
            this.setHeader(name, s);
        }
    }

    private void addAddressHeader(String name, Address[] addresses) throws MessagingException {
        Address[] anew;
        if (addresses == null || addresses.length == 0) {
            return;
        }
        Address[] a = this.getAddressHeader(name);
        if (a == null || a.length == 0) {
            anew = addresses;
        } else {
            anew = new Address[a.length + addresses.length];
            System.arraycopy(a, 0, anew, 0, a.length);
            System.arraycopy(addresses, 0, anew, a.length, addresses.length);
        }
        String s = InternetAddress.toString(anew, name.length() + 2);
        if (s == null) {
            return;
        }
        this.setHeader(name, s);
    }

    @Override
    public String getSubject() throws MessagingException {
        String rawvalue = this.getHeader("Subject", null);
        if (rawvalue == null) {
            return null;
        }
        try {
            return MimeUtility.decodeText(MimeUtility.unfold(rawvalue));
        }
        catch (UnsupportedEncodingException ex) {
            return rawvalue;
        }
    }

    @Override
    public void setSubject(String subject) throws MessagingException {
        this.setSubject(subject, null);
    }

    public void setSubject(String subject, String charset) throws MessagingException {
        if (subject == null) {
            this.removeHeader("Subject");
        } else {
            try {
                this.setHeader("Subject", MimeUtility.fold(9, MimeUtility.encodeText(subject, charset, null)));
            }
            catch (UnsupportedEncodingException uex) {
                throw new MessagingException("Encoding error", uex);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Date getSentDate() throws MessagingException {
        String s = this.getHeader("Date", null);
        if (s != null) {
            try {
                MailDateFormat mailDateFormat = MimeMessage.mailDateFormat;
                synchronized (mailDateFormat) {
                    return MimeMessage.mailDateFormat.parse(s);
                }
            }
            catch (ParseException pex) {
                return null;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setSentDate(Date d) throws MessagingException {
        if (d == null) {
            this.removeHeader("Date");
        } else {
            MailDateFormat mailDateFormat = MimeMessage.mailDateFormat;
            synchronized (mailDateFormat) {
                this.setHeader("Date", MimeMessage.mailDateFormat.format(d));
            }
        }
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        return null;
    }

    @Override
    public int getSize() throws MessagingException {
        if (this.content != null) {
            return this.content.length;
        }
        if (this.contentStream != null) {
            try {
                int size = this.contentStream.available();
                if (size > 0) {
                    return size;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return -1;
    }

    @Override
    public int getLineCount() throws MessagingException {
        return -1;
    }

    @Override
    public String getContentType() throws MessagingException {
        String s = this.getHeader("Content-Type", null);
        if ((s = MimeUtil.cleanContentType(this, s)) == null) {
            return "text/plain";
        }
        return s;
    }

    @Override
    public boolean isMimeType(String mimeType) throws MessagingException {
        return MimeBodyPart.isMimeType(this, mimeType);
    }

    @Override
    public String getDisposition() throws MessagingException {
        return MimeBodyPart.getDisposition(this);
    }

    @Override
    public void setDisposition(String disposition) throws MessagingException {
        MimeBodyPart.setDisposition(this, disposition);
    }

    @Override
    public String getEncoding() throws MessagingException {
        return MimeBodyPart.getEncoding(this);
    }

    @Override
    public String getContentID() throws MessagingException {
        return this.getHeader("Content-Id", null);
    }

    public void setContentID(String cid) throws MessagingException {
        if (cid == null) {
            this.removeHeader("Content-ID");
        } else {
            this.setHeader("Content-ID", cid);
        }
    }

    @Override
    public String getContentMD5() throws MessagingException {
        return this.getHeader("Content-MD5", null);
    }

    @Override
    public void setContentMD5(String md5) throws MessagingException {
        this.setHeader("Content-MD5", md5);
    }

    @Override
    public String getDescription() throws MessagingException {
        return MimeBodyPart.getDescription(this);
    }

    @Override
    public void setDescription(String description) throws MessagingException {
        this.setDescription(description, null);
    }

    public void setDescription(String description, String charset) throws MessagingException {
        MimeBodyPart.setDescription(this, description, charset);
    }

    @Override
    public String[] getContentLanguage() throws MessagingException {
        return MimeBodyPart.getContentLanguage(this);
    }

    @Override
    public void setContentLanguage(String[] languages) throws MessagingException {
        MimeBodyPart.setContentLanguage(this, languages);
    }

    public String getMessageID() throws MessagingException {
        return this.getHeader("Message-ID", null);
    }

    @Override
    public String getFileName() throws MessagingException {
        return MimeBodyPart.getFileName(this);
    }

    @Override
    public void setFileName(String filename) throws MessagingException {
        MimeBodyPart.setFileName(this, filename);
    }

    private String getHeaderName(Message.RecipientType type) throws MessagingException {
        String headerName;
        if (type == Message.RecipientType.TO) {
            headerName = "To";
        } else if (type == Message.RecipientType.CC) {
            headerName = "Cc";
        } else if (type == Message.RecipientType.BCC) {
            headerName = "Bcc";
        } else if (type == RecipientType.NEWSGROUPS) {
            headerName = "Newsgroups";
        } else {
            throw new MessagingException("Invalid Recipient Type");
        }
        return headerName;
    }

    @Override
    public InputStream getInputStream() throws IOException, MessagingException {
        return this.getDataHandler().getInputStream();
    }

    protected InputStream getContentStream() throws MessagingException {
        if (this.contentStream != null) {
            return ((SharedInputStream)((Object)this.contentStream)).newStream(0L, -1L);
        }
        if (this.content != null) {
            return new SharedByteArrayInputStream(this.content);
        }
        throw new MessagingException("No MimeMessage content");
    }

    public InputStream getRawInputStream() throws MessagingException {
        return this.getContentStream();
    }

    @Override
    public synchronized DataHandler getDataHandler() throws MessagingException {
        if (this.dh == null) {
            this.dh = new MimeBodyPart.MimePartDataHandler(this);
        }
        return this.dh;
    }

    @Override
    public Object getContent() throws IOException, MessagingException {
        Object c;
        if (this.cachedContent != null) {
            return this.cachedContent;
        }
        try {
            c = this.getDataHandler().getContent();
        }
        catch (FolderClosedIOException fex) {
            throw new FolderClosedException(fex.getFolder(), fex.getMessage());
        }
        catch (MessageRemovedIOException mex) {
            throw new MessageRemovedException(mex.getMessage());
        }
        if (MimeBodyPart.cacheMultipart && (c instanceof Multipart || c instanceof Message) && (this.content != null || this.contentStream != null)) {
            this.cachedContent = c;
            if (c instanceof MimeMultipart) {
                ((MimeMultipart)c).parse();
            }
        }
        return c;
    }

    @Override
    public synchronized void setDataHandler(DataHandler dh) throws MessagingException {
        this.dh = dh;
        this.cachedContent = null;
        MimeBodyPart.invalidateContentHeaders(this);
    }

    @Override
    public void setContent(Object o, String type) throws MessagingException {
        if (o instanceof Multipart) {
            this.setContent((Multipart)o);
        } else {
            this.setDataHandler(new DataHandler(o, type));
        }
    }

    @Override
    public void setText(String text) throws MessagingException {
        this.setText(text, null);
    }

    @Override
    public void setText(String text, String charset) throws MessagingException {
        MimeBodyPart.setText(this, text, charset, "plain");
    }

    @Override
    public void setText(String text, String charset, String subtype) throws MessagingException {
        MimeBodyPart.setText(this, text, charset, subtype);
    }

    @Override
    public void setContent(Multipart mp) throws MessagingException {
        this.setDataHandler(new DataHandler((Object)mp, mp.getContentType()));
        mp.setParent(this);
    }

    @Override
    public Message reply(boolean replyToAll) throws MessagingException {
        return this.reply(replyToAll, true);
    }

    public Message reply(boolean replyToAll, boolean setAnswered) throws MessagingException {
        String refs;
        String msgId;
        MimeMessage reply = this.createMimeMessage(this.session);
        String subject = this.getHeader("Subject", null);
        if (subject != null) {
            if (!subject.regionMatches(true, 0, "Re: ", 0, 4)) {
                subject = "Re: " + subject;
            }
            reply.setHeader("Subject", subject);
        }
        Address[] a = this.getReplyTo();
        reply.setRecipients(Message.RecipientType.TO, a);
        if (replyToAll) {
            ArrayList<Address> v = new ArrayList<Address>();
            InternetAddress me = InternetAddress.getLocalAddress(this.session);
            if (me != null) {
                v.add(me);
            }
            String alternates = null;
            if (this.session != null) {
                alternates = this.session.getProperty("mail.alternates");
            }
            if (alternates != null) {
                this.eliminateDuplicates(v, InternetAddress.parse(alternates, false));
            }
            Object replyallccStr = null;
            boolean replyallcc = false;
            if (this.session != null) {
                replyallcc = PropUtil.getBooleanSessionProperty(this.session, "mail.replyallcc", false);
            }
            this.eliminateDuplicates(v, a);
            a = this.getRecipients(Message.RecipientType.TO);
            a = this.eliminateDuplicates(v, a);
            if (a != null && a.length > 0) {
                if (replyallcc) {
                    reply.addRecipients(Message.RecipientType.CC, a);
                } else {
                    reply.addRecipients(Message.RecipientType.TO, a);
                }
            }
            a = this.getRecipients(Message.RecipientType.CC);
            if ((a = this.eliminateDuplicates(v, a)) != null && a.length > 0) {
                reply.addRecipients(Message.RecipientType.CC, a);
            }
            if ((a = this.getRecipients(RecipientType.NEWSGROUPS)) != null && a.length > 0) {
                reply.setRecipients((Message.RecipientType)RecipientType.NEWSGROUPS, a);
            }
        }
        if ((msgId = this.getHeader("Message-Id", null)) != null) {
            reply.setHeader("In-Reply-To", msgId);
        }
        if ((refs = this.getHeader("References", " ")) == null) {
            refs = this.getHeader("In-Reply-To", " ");
        }
        if (msgId != null) {
            refs = refs != null ? MimeUtility.unfold(refs) + " " + msgId : msgId;
        }
        if (refs != null) {
            reply.setHeader("References", MimeUtility.fold(12, refs));
        }
        if (setAnswered) {
            try {
                this.setFlags(answeredFlag, true);
            }
            catch (MessagingException messagingException) {
                // empty catch block
            }
        }
        return reply;
    }

    private Address[] eliminateDuplicates(List<Address> v, Address[] addrs) {
        int j;
        if (addrs == null) {
            return null;
        }
        int gone = 0;
        for (int i = 0; i < addrs.length; ++i) {
            boolean found = false;
            for (j = 0; j < v.size(); ++j) {
                if (!((InternetAddress)v.get(j)).equals(addrs[i])) continue;
                found = true;
                ++gone;
                addrs[i] = null;
                break;
            }
            if (found) continue;
            v.add(addrs[i]);
        }
        if (gone != 0) {
            Address[] a = addrs instanceof InternetAddress[] ? new InternetAddress[addrs.length - gone] : new Address[addrs.length - gone];
            j = 0;
            for (int i = 0; i < addrs.length; ++i) {
                if (addrs[i] == null) continue;
                a[j++] = addrs[i];
            }
            addrs = a;
        }
        return addrs;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException, MessagingException {
        this.writeTo(os, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeTo(OutputStream os, String[] ignoreList) throws IOException, MessagingException {
        block9: {
            if (!this.saved) {
                this.saveChanges();
            }
            if (this.modified) {
                MimeBodyPart.writeTo(this, os, ignoreList);
                return;
            }
            Enumeration hdrLines = this.getNonMatchingHeaderLines(ignoreList);
            LineOutputStream los = new LineOutputStream(os);
            while (hdrLines.hasMoreElements()) {
                los.writeln((String)hdrLines.nextElement());
            }
            los.writeln();
            if (this.content == null) {
                byte[] buf;
                block8: {
                    InputStream is = null;
                    buf = new byte[8192];
                    try {
                        int len;
                        is = this.getContentStream();
                        while ((len = is.read(buf)) > 0) {
                            os.write(buf, 0, len);
                        }
                        Object var9_8 = null;
                        if (is == null) break block8;
                    }
                    catch (Throwable throwable) {
                        Object var9_9 = null;
                        if (is != null) {
                            is.close();
                        }
                        buf = null;
                        throw throwable;
                    }
                    is.close();
                }
                buf = null;
                {
                    break block9;
                }
            }
            os.write(this.content);
        }
        os.flush();
    }

    @Override
    public String[] getHeader(String name) throws MessagingException {
        return this.headers.getHeader(name);
    }

    @Override
    public String getHeader(String name, String delimiter) throws MessagingException {
        return this.headers.getHeader(name, delimiter);
    }

    @Override
    public void setHeader(String name, String value) throws MessagingException {
        this.headers.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) throws MessagingException {
        this.headers.addHeader(name, value);
    }

    @Override
    public void removeHeader(String name) throws MessagingException {
        this.headers.removeHeader(name);
    }

    @Override
    public Enumeration getAllHeaders() throws MessagingException {
        return this.headers.getAllHeaders();
    }

    @Override
    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        return this.headers.getMatchingHeaders(names);
    }

    @Override
    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaders(names);
    }

    @Override
    public void addHeaderLine(String line) throws MessagingException {
        this.headers.addHeaderLine(line);
    }

    @Override
    public Enumeration getAllHeaderLines() throws MessagingException {
        return this.headers.getAllHeaderLines();
    }

    @Override
    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        return this.headers.getMatchingHeaderLines(names);
    }

    @Override
    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaderLines(names);
    }

    @Override
    public synchronized Flags getFlags() throws MessagingException {
        return (Flags)this.flags.clone();
    }

    @Override
    public synchronized boolean isSet(Flags.Flag flag) throws MessagingException {
        return this.flags.contains(flag);
    }

    @Override
    public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
        if (set) {
            this.flags.add(flag);
        } else {
            this.flags.remove(flag);
        }
    }

    @Override
    public void saveChanges() throws MessagingException {
        this.modified = true;
        this.saved = true;
        this.updateHeaders();
    }

    protected void updateMessageID() throws MessagingException {
        this.setHeader("Message-ID", "<" + UniqueValue.getUniqueMessageIDValue(this.session) + ">");
    }

    protected synchronized void updateHeaders() throws MessagingException {
        MimeBodyPart.updateHeaders(this);
        this.setHeader("MIME-Version", "1.0");
        this.updateMessageID();
        if (this.cachedContent != null) {
            this.dh = new DataHandler(this.cachedContent, this.getContentType());
            this.cachedContent = null;
            this.content = null;
            if (this.contentStream != null) {
                try {
                    this.contentStream.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
            this.contentStream = null;
        }
    }

    protected InternetHeaders createInternetHeaders(InputStream is) throws MessagingException {
        return new InternetHeaders(is);
    }

    protected MimeMessage createMimeMessage(Session session) throws MessagingException {
        return new MimeMessage(session);
    }

    public static class RecipientType
    extends Message.RecipientType {
        private static final long serialVersionUID = -5468290701714395543L;
        public static final RecipientType NEWSGROUPS = new RecipientType("Newsgroups");

        protected RecipientType(String type) {
            super(type);
        }

        protected Object readResolve() throws ObjectStreamException {
            if (this.type.equals("Newsgroups")) {
                return NEWSGROUPS;
            }
            return super.readResolve();
        }
    }
}

