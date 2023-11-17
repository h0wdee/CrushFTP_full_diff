/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 *  javax.activation.DataSource
 *  javax.activation.FileDataSource
 */
package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.MessageRemovedIOException;
import com.sun.mail.util.MimeUtil;
import com.sun.mail.util.PropUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.EncodingAware;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimePartDataSource;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;
import javax.mail.internet.SharedInputStream;

public class MimeBodyPart
extends BodyPart
implements MimePart {
    private static final boolean setDefaultTextCharset = PropUtil.getBooleanSystemProperty("mail.mime.setdefaulttextcharset", true);
    private static final boolean setContentTypeFileName = PropUtil.getBooleanSystemProperty("mail.mime.setcontenttypefilename", true);
    private static final boolean encodeFileName = PropUtil.getBooleanSystemProperty("mail.mime.encodefilename", false);
    private static final boolean decodeFileName = PropUtil.getBooleanSystemProperty("mail.mime.decodefilename", false);
    private static final boolean ignoreMultipartEncoding = PropUtil.getBooleanSystemProperty("mail.mime.ignoremultipartencoding", true);
    static final boolean cacheMultipart = PropUtil.getBooleanSystemProperty("mail.mime.cachemultipart", true);
    protected DataHandler dh;
    protected byte[] content;
    protected InputStream contentStream;
    protected InternetHeaders headers;
    protected Object cachedContent;

    public MimeBodyPart() {
        this.headers = new InternetHeaders();
    }

    public MimeBodyPart(InputStream is) throws MessagingException {
        if (!(is instanceof ByteArrayInputStream || is instanceof BufferedInputStream || is instanceof SharedInputStream)) {
            is = new BufferedInputStream(is);
        }
        this.headers = new InternetHeaders(is);
        if (is instanceof SharedInputStream) {
            SharedInputStream sis = (SharedInputStream)((Object)is);
            this.contentStream = sis.newStream(sis.getPosition(), -1L);
        } else {
            try {
                this.content = ASCIIUtility.getBytes(is);
            }
            catch (IOException ioex) {
                throw new MessagingException("Error reading input stream", ioex);
            }
        }
    }

    public MimeBodyPart(InternetHeaders headers, byte[] content) throws MessagingException {
        this.headers = headers;
        this.content = content;
    }

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

    public int getLineCount() throws MessagingException {
        return -1;
    }

    public String getContentType() throws MessagingException {
        String s = this.getHeader("Content-Type", null);
        if ((s = MimeUtil.cleanContentType(this, s)) == null) {
            s = "text/plain";
        }
        return s;
    }

    public boolean isMimeType(String mimeType) throws MessagingException {
        return MimeBodyPart.isMimeType(this, mimeType);
    }

    public String getDisposition() throws MessagingException {
        return MimeBodyPart.getDisposition(this);
    }

    public void setDisposition(String disposition) throws MessagingException {
        MimeBodyPart.setDisposition(this, disposition);
    }

    public String getEncoding() throws MessagingException {
        return MimeBodyPart.getEncoding(this);
    }

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

    public String getContentMD5() throws MessagingException {
        return this.getHeader("Content-MD5", null);
    }

    public void setContentMD5(String md5) throws MessagingException {
        this.setHeader("Content-MD5", md5);
    }

    public String[] getContentLanguage() throws MessagingException {
        return MimeBodyPart.getContentLanguage(this);
    }

    public void setContentLanguage(String[] languages) throws MessagingException {
        MimeBodyPart.setContentLanguage(this, languages);
    }

    public String getDescription() throws MessagingException {
        return MimeBodyPart.getDescription(this);
    }

    public void setDescription(String description) throws MessagingException {
        this.setDescription(description, null);
    }

    public void setDescription(String description, String charset) throws MessagingException {
        MimeBodyPart.setDescription(this, description, charset);
    }

    public String getFileName() throws MessagingException {
        return MimeBodyPart.getFileName(this);
    }

    public void setFileName(String filename) throws MessagingException {
        MimeBodyPart.setFileName(this, filename);
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        return this.getDataHandler().getInputStream();
    }

    protected InputStream getContentStream() throws MessagingException {
        if (this.contentStream != null) {
            return ((SharedInputStream)((Object)this.contentStream)).newStream(0L, -1L);
        }
        if (this.content != null) {
            return new ByteArrayInputStream(this.content);
        }
        throw new MessagingException("No MimeBodyPart content");
    }

    public InputStream getRawInputStream() throws MessagingException {
        return this.getContentStream();
    }

    public DataHandler getDataHandler() throws MessagingException {
        if (this.dh == null) {
            this.dh = new MimePartDataHandler(this);
        }
        return this.dh;
    }

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
        if (cacheMultipart && (c instanceof Multipart || c instanceof Message) && (this.content != null || this.contentStream != null)) {
            this.cachedContent = c;
            if (c instanceof MimeMultipart) {
                ((MimeMultipart)c).parse();
            }
        }
        return c;
    }

    public void setDataHandler(DataHandler dh) throws MessagingException {
        this.dh = dh;
        this.cachedContent = null;
        MimeBodyPart.invalidateContentHeaders(this);
    }

    public void setContent(Object o, String type) throws MessagingException {
        if (o instanceof Multipart) {
            this.setContent((Multipart)o);
        } else {
            this.setDataHandler(new DataHandler(o, type));
        }
    }

    public void setText(String text) throws MessagingException {
        this.setText(text, null);
    }

    public void setText(String text, String charset) throws MessagingException {
        MimeBodyPart.setText(this, text, charset, "plain");
    }

    public void setText(String text, String charset, String subtype) throws MessagingException {
        MimeBodyPart.setText(this, text, charset, subtype);
    }

    public void setContent(Multipart mp) throws MessagingException {
        this.setDataHandler(new DataHandler((Object)mp, mp.getContentType()));
        mp.setParent(this);
    }

    public void attachFile(File file) throws IOException, MessagingException {
        FileDataSource fds = new FileDataSource(file);
        this.setDataHandler(new DataHandler((DataSource)fds));
        this.setFileName(fds.getName());
        this.setDisposition("attachment");
    }

    public void attachFile(String file) throws IOException, MessagingException {
        File f = new File(file);
        this.attachFile(f);
    }

    public void attachFile(File file, String contentType, String encoding) throws IOException, MessagingException {
        EncodedFileDataSource fds = new EncodedFileDataSource(file, contentType, encoding);
        this.setDataHandler(new DataHandler((DataSource)fds));
        this.setFileName(fds.getName());
        this.setDisposition("attachment");
    }

    public void attachFile(String file, String contentType, String encoding) throws IOException, MessagingException {
        this.attachFile(new File(file), contentType, encoding);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveFile(File file) throws IOException, MessagingException {
        BufferedOutputStream out = null;
        InputStream in = null;
        try {
            int len;
            out = new BufferedOutputStream(new FileOutputStream(file));
            in = this.getInputStream();
            byte[] buf = new byte[8192];
            while ((len = in.read(buf)) > 0) {
                ((OutputStream)out).write(buf, 0, len);
            }
            Object var7_6 = null;
        }
        catch (Throwable throwable) {
            Object var7_7 = null;
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            try {
                if (out != null) {
                    ((OutputStream)out).close();
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
        }
        catch (IOException iOException) {
            // empty catch block
        }
        try {
            if (out != null) {
                ((OutputStream)out).close();
            }
        }
        catch (IOException iOException) {}
    }

    public void saveFile(String file) throws IOException, MessagingException {
        File f = new File(file);
        this.saveFile(f);
    }

    public void writeTo(OutputStream os) throws IOException, MessagingException {
        MimeBodyPart.writeTo(this, os, null);
    }

    public String[] getHeader(String name) throws MessagingException {
        return this.headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        return this.headers.getHeader(name, delimiter);
    }

    public void setHeader(String name, String value) throws MessagingException {
        this.headers.setHeader(name, value);
    }

    public void addHeader(String name, String value) throws MessagingException {
        this.headers.addHeader(name, value);
    }

    public void removeHeader(String name) throws MessagingException {
        this.headers.removeHeader(name);
    }

    public Enumeration getAllHeaders() throws MessagingException {
        return this.headers.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        return this.headers.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        this.headers.addHeaderLine(line);
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        return this.headers.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        return this.headers.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaderLines(names);
    }

    protected void updateHeaders() throws MessagingException {
        MimeBodyPart.updateHeaders(this);
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

    static boolean isMimeType(MimePart part, String mimeType) throws MessagingException {
        String type = part.getContentType();
        try {
            return new ContentType(type).match(mimeType);
        }
        catch (ParseException ex) {
            try {
                int i = type.indexOf(59);
                if (i > 0) {
                    return new ContentType(type.substring(0, i)).match(mimeType);
                }
            }
            catch (ParseException parseException) {
                // empty catch block
            }
            return type.equalsIgnoreCase(mimeType);
        }
    }

    static void setText(MimePart part, String text, String charset, String subtype) throws MessagingException {
        if (charset == null) {
            charset = MimeUtility.checkAscii(text) != 1 ? MimeUtility.getDefaultMIMECharset() : "us-ascii";
        }
        part.setContent(text, "text/" + subtype + "; charset=" + MimeUtility.quote(charset, "()<>@,;:\\\"\t []/?="));
    }

    static String getDisposition(MimePart part) throws MessagingException {
        String s = part.getHeader("Content-Disposition", null);
        if (s == null) {
            return null;
        }
        ContentDisposition cd = new ContentDisposition(s);
        return cd.getDisposition();
    }

    static void setDisposition(MimePart part, String disposition) throws MessagingException {
        if (disposition == null) {
            part.removeHeader("Content-Disposition");
        } else {
            String s = part.getHeader("Content-Disposition", null);
            if (s != null) {
                ContentDisposition cd = new ContentDisposition(s);
                cd.setDisposition(disposition);
                disposition = cd.toString();
            }
            part.setHeader("Content-Disposition", disposition);
        }
    }

    static String getDescription(MimePart part) throws MessagingException {
        String rawvalue = part.getHeader("Content-Description", null);
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

    static void setDescription(MimePart part, String description, String charset) throws MessagingException {
        if (description == null) {
            part.removeHeader("Content-Description");
            return;
        }
        try {
            part.setHeader("Content-Description", MimeUtility.fold(21, MimeUtility.encodeText(description, charset, null)));
        }
        catch (UnsupportedEncodingException uex) {
            throw new MessagingException("Encoding error", uex);
        }
    }

    static String getFileName(MimePart part) throws MessagingException {
        String filename = null;
        String s = part.getHeader("Content-Disposition", null);
        if (s != null) {
            ContentDisposition cd = new ContentDisposition(s);
            filename = cd.getParameter("filename");
        }
        if (filename == null) {
            s = part.getHeader("Content-Type", null);
            if ((s = MimeUtil.cleanContentType(part, s)) != null) {
                try {
                    ContentType ct = new ContentType(s);
                    filename = ct.getParameter("name");
                }
                catch (ParseException ct) {
                    // empty catch block
                }
            }
        }
        if (decodeFileName && filename != null) {
            try {
                filename = MimeUtility.decodeText(filename);
            }
            catch (UnsupportedEncodingException ex) {
                throw new MessagingException("Can't decode filename", ex);
            }
        }
        return filename;
    }

    static void setFileName(MimePart part, String name) throws MessagingException {
        String s;
        if (encodeFileName && name != null) {
            try {
                name = MimeUtility.encodeText(name);
            }
            catch (UnsupportedEncodingException ex) {
                throw new MessagingException("Can't encode filename", ex);
            }
        }
        ContentDisposition cd = new ContentDisposition((s = part.getHeader("Content-Disposition", null)) == null ? "attachment" : s);
        String charset = MimeUtility.getDefaultMIMECharset();
        ParameterList p = cd.getParameterList();
        if (p == null) {
            p = new ParameterList();
            cd.setParameterList(p);
        }
        if (encodeFileName) {
            p.setLiteral("filename", name);
        } else {
            p.set("filename", name, charset);
        }
        part.setHeader("Content-Disposition", cd.toString());
        if (setContentTypeFileName) {
            s = part.getHeader("Content-Type", null);
            if ((s = MimeUtil.cleanContentType(part, s)) != null) {
                try {
                    ContentType cType = new ContentType(s);
                    p = cType.getParameterList();
                    if (p == null) {
                        p = new ParameterList();
                        cType.setParameterList(p);
                    }
                    if (encodeFileName) {
                        p.setLiteral("name", name);
                    } else {
                        p.set("name", name, charset);
                    }
                    part.setHeader("Content-Type", cType.toString());
                }
                catch (ParseException parseException) {
                    // empty catch block
                }
            }
        }
    }

    static String[] getContentLanguage(MimePart part) throws MessagingException {
        HeaderTokenizer.Token tk;
        int tkType;
        String s = part.getHeader("Content-Language", null);
        if (s == null) {
            return null;
        }
        HeaderTokenizer h = new HeaderTokenizer(s, "()<>@,;:\\\"\t []/?=");
        ArrayList<String> v = new ArrayList<String>();
        while ((tkType = (tk = h.next()).getType()) != -4) {
            if (tkType != -1) continue;
            v.add(tk.getValue());
        }
        if (v.isEmpty()) {
            return null;
        }
        String[] language = new String[v.size()];
        v.toArray(language);
        return language;
    }

    static void setContentLanguage(MimePart part, String[] languages) throws MessagingException {
        StringBuffer sb = new StringBuffer(languages[0]);
        int len = "Content-Language".length() + 2 + languages[0].length();
        for (int i = 1; i < languages.length; ++i) {
            sb.append(',');
            if (++len > 76) {
                sb.append("\r\n\t");
                len = 8;
            }
            sb.append(languages[i]);
            len += languages[i].length();
        }
        part.setHeader("Content-Language", sb.toString());
    }

    static String getEncoding(MimePart part) throws MessagingException {
        HeaderTokenizer.Token tk;
        int tkType;
        String s = part.getHeader("Content-Transfer-Encoding", null);
        if (s == null) {
            return null;
        }
        if ((s = s.trim()).equalsIgnoreCase("7bit") || s.equalsIgnoreCase("8bit") || s.equalsIgnoreCase("quoted-printable") || s.equalsIgnoreCase("binary") || s.equalsIgnoreCase("base64")) {
            return s;
        }
        HeaderTokenizer h = new HeaderTokenizer(s, "()<>@,;:\\\"\t []/?=");
        while ((tkType = (tk = h.next()).getType()) != -4) {
            if (tkType != -1) continue;
            return tk.getValue();
        }
        return s;
    }

    static void setEncoding(MimePart part, String encoding) throws MessagingException {
        part.setHeader("Content-Transfer-Encoding", encoding);
    }

    static String restrictEncoding(MimePart part, String encoding) throws MessagingException {
        if (!ignoreMultipartEncoding || encoding == null) {
            return encoding;
        }
        if (encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit") || encoding.equalsIgnoreCase("binary")) {
            return encoding;
        }
        String type = part.getContentType();
        if (type == null) {
            return encoding;
        }
        try {
            ContentType cType = new ContentType(type);
            if (cType.match("multipart/*")) {
                return null;
            }
            if (cType.match("message/*") && !PropUtil.getBooleanSystemProperty("mail.mime.allowencodedmessages", false)) {
                return null;
            }
        }
        catch (ParseException parseException) {
            // empty catch block
        }
        return encoding;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static void updateHeaders(MimePart part) throws MessagingException {
        DataHandler dh = part.getDataHandler();
        if (dh == null) {
            return;
        }
        try {
            ContentDisposition cd;
            String filename;
            String s;
            String type = dh.getContentType();
            boolean composite = false;
            boolean needCTHeader = part.getHeader("Content-Type") == null;
            ContentType cType = new ContentType(type);
            if (cType.match("multipart/*")) {
                Object o;
                composite = true;
                if (part instanceof MimeBodyPart) {
                    MimeBodyPart mbp = (MimeBodyPart)part;
                    o = mbp.cachedContent != null ? mbp.cachedContent : dh.getContent();
                } else if (part instanceof MimeMessage) {
                    MimeMessage msg = (MimeMessage)part;
                    o = msg.cachedContent != null ? msg.cachedContent : dh.getContent();
                } else {
                    o = dh.getContent();
                }
                if (!(o instanceof MimeMultipart)) throw new MessagingException("MIME part of type \"" + type + "\" contains object of type " + o.getClass().getName() + " instead of MimeMultipart");
                ((MimeMultipart)o).updateHeaders();
            } else if (cType.match("message/rfc822")) {
                composite = true;
            }
            if (dh instanceof MimePartDataHandler) {
                String enc;
                MimePartDataHandler mdh = (MimePartDataHandler)dh;
                MimePart mpart = mdh.getPart();
                if (mpart == part) return;
                if (needCTHeader) {
                    part.setHeader("Content-Type", mpart.getContentType());
                }
                if ((enc = mpart.getEncoding()) != null) {
                    MimeBodyPart.setEncoding(part, enc);
                    return;
                }
            }
            if (!composite) {
                if (part.getHeader("Content-Transfer-Encoding") == null) {
                    MimeBodyPart.setEncoding(part, MimeUtility.getEncoding(dh));
                }
                if (needCTHeader && setDefaultTextCharset && cType.match("text/*") && cType.getParameter("charset") == null) {
                    String enc = part.getEncoding();
                    String charset = enc != null && enc.equalsIgnoreCase("7bit") ? "us-ascii" : MimeUtility.getDefaultMIMECharset();
                    cType.setParameter("charset", charset);
                    type = cType.toString();
                }
            }
            if (!needCTHeader) return;
            if (setContentTypeFileName && (s = part.getHeader("Content-Disposition", null)) != null && (filename = (cd = new ContentDisposition(s)).getParameter("filename")) != null) {
                ParameterList p = cType.getParameterList();
                if (p == null) {
                    p = new ParameterList();
                    cType.setParameterList(p);
                }
                if (encodeFileName) {
                    p.setLiteral("name", MimeUtility.encodeText(filename));
                } else {
                    p.set("name", filename, MimeUtility.getDefaultMIMECharset());
                }
                type = cType.toString();
            }
            part.setHeader("Content-Type", type);
            return;
        }
        catch (IOException ex) {
            throw new MessagingException("IOException updating headers", ex);
        }
    }

    static void invalidateContentHeaders(MimePart part) throws MessagingException {
        part.removeHeader("Content-Type");
        part.removeHeader("Content-Transfer-Encoding");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static void writeTo(MimePart part, OutputStream os, String[] ignoreList) throws IOException, MessagingException {
        byte[] buf;
        block8: {
            LineOutputStream los = null;
            los = os instanceof LineOutputStream ? (LineOutputStream)os : new LineOutputStream(os);
            Enumeration hdrLines = part.getNonMatchingHeaderLines(ignoreList);
            while (hdrLines.hasMoreElements()) {
                los.writeln((String)hdrLines.nextElement());
            }
            los.writeln();
            InputStream is = null;
            buf = null;
            try {
                MimePartDataHandler mpdh;
                MimePart mpart;
                DataHandler dh = part.getDataHandler();
                if (dh instanceof MimePartDataHandler && (mpart = (mpdh = (MimePartDataHandler)dh).getPart()).getEncoding() != null) {
                    is = mpdh.getContentStream();
                }
                if (is != null) {
                    int len;
                    buf = new byte[8192];
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                } else {
                    os = MimeUtility.encode(os, MimeBodyPart.restrictEncoding(part, part.getEncoding()));
                    part.getDataHandler().writeTo(os);
                }
                Object var11_11 = null;
                if (is == null) break block8;
            }
            catch (Throwable throwable) {
                Object var11_12 = null;
                if (is != null) {
                    is.close();
                }
                buf = null;
                throw throwable;
            }
            is.close();
        }
        buf = null;
        os.flush();
    }

    static class MimePartDataHandler
    extends DataHandler {
        MimePart part;

        public MimePartDataHandler(MimePart part) {
            super((DataSource)new MimePartDataSource(part));
            this.part = part;
        }

        InputStream getContentStream() throws MessagingException {
            InputStream is = null;
            if (this.part instanceof MimeBodyPart) {
                MimeBodyPart mbp = (MimeBodyPart)this.part;
                is = mbp.getContentStream();
            } else if (this.part instanceof MimeMessage) {
                MimeMessage msg = (MimeMessage)this.part;
                is = msg.getContentStream();
            }
            return is;
        }

        MimePart getPart() {
            return this.part;
        }
    }

    private static class EncodedFileDataSource
    extends FileDataSource
    implements EncodingAware {
        private String contentType;
        private String encoding;

        public EncodedFileDataSource(File file, String contentType, String encoding) {
            super(file);
            this.contentType = contentType;
            this.encoding = encoding;
        }

        public String getContentType() {
            return this.contentType != null ? this.contentType : super.getContentType();
        }

        public String getEncoding() {
            return this.encoding;
        }
    }
}

