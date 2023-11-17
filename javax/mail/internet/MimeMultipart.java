/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataSource
 */
package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.PropUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.SharedInputStream;
import javax.mail.internet.UniqueValue;

public class MimeMultipart
extends Multipart {
    protected DataSource ds = null;
    protected boolean parsed = true;
    protected boolean complete = true;
    protected String preamble = null;
    protected boolean ignoreMissingEndBoundary = true;
    protected boolean ignoreMissingBoundaryParameter = true;
    protected boolean ignoreExistingBoundaryParameter = false;
    protected boolean allowEmpty = false;

    public MimeMultipart() {
        this("mixed");
    }

    public MimeMultipart(String subtype) {
        String boundary = UniqueValue.getUniqueBoundaryValue();
        ContentType cType = new ContentType("multipart", subtype, null);
        cType.setParameter("boundary", boundary);
        this.contentType = cType.toString();
        this.initializeProperties();
    }

    public MimeMultipart(BodyPart ... parts) throws MessagingException {
        this();
        for (BodyPart bp : parts) {
            super.addBodyPart(bp);
        }
    }

    public MimeMultipart(String subtype, BodyPart ... parts) throws MessagingException {
        this(subtype);
        for (BodyPart bp : parts) {
            super.addBodyPart(bp);
        }
    }

    public MimeMultipart(DataSource ds) throws MessagingException {
        if (ds instanceof MessageAware) {
            MessageContext mc = ((MessageAware)ds).getMessageContext();
            this.setParent(mc.getPart());
        }
        if (ds instanceof MultipartDataSource) {
            this.setMultipartDataSource((MultipartDataSource)ds);
            return;
        }
        this.parsed = false;
        this.ds = ds;
        this.contentType = ds.getContentType();
    }

    protected void initializeProperties() {
        this.ignoreMissingEndBoundary = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingendboundary", true);
        this.ignoreMissingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingboundaryparameter", true);
        this.ignoreExistingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoreexistingboundaryparameter", false);
        this.allowEmpty = PropUtil.getBooleanSystemProperty("mail.mime.multipart.allowempty", false);
    }

    public synchronized void setSubType(String subtype) throws MessagingException {
        ContentType cType = new ContentType(this.contentType);
        cType.setSubType(subtype);
        this.contentType = cType.toString();
    }

    public synchronized int getCount() throws MessagingException {
        this.parse();
        return super.getCount();
    }

    public synchronized BodyPart getBodyPart(int index) throws MessagingException {
        this.parse();
        return super.getBodyPart(index);
    }

    public synchronized BodyPart getBodyPart(String CID) throws MessagingException {
        this.parse();
        int count = this.getCount();
        for (int i = 0; i < count; ++i) {
            MimeBodyPart part = (MimeBodyPart)this.getBodyPart(i);
            String s = part.getContentID();
            if (s == null || !s.equals(CID)) continue;
            return part;
        }
        return null;
    }

    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        this.parse();
        return super.removeBodyPart(part);
    }

    public void removeBodyPart(int index) throws MessagingException {
        this.parse();
        super.removeBodyPart(index);
    }

    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        this.parse();
        super.addBodyPart(part);
    }

    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        this.parse();
        super.addBodyPart(part, index);
    }

    public synchronized boolean isComplete() throws MessagingException {
        this.parse();
        return this.complete;
    }

    public synchronized String getPreamble() throws MessagingException {
        this.parse();
        return this.preamble;
    }

    public synchronized void setPreamble(String preamble) throws MessagingException {
        this.preamble = preamble;
    }

    protected synchronized void updateHeaders() throws MessagingException {
        this.parse();
        for (int i = 0; i < this.parts.size(); ++i) {
            ((MimeBodyPart)this.parts.elementAt(i)).updateHeaders();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public synchronized void writeTo(OutputStream os) throws IOException, MessagingException {
        this.parse();
        String boundary = "--" + new ContentType(this.contentType).getParameter("boundary");
        LineOutputStream los = new LineOutputStream(os);
        if (this.preamble != null) {
            byte[] pb = ASCIIUtility.getBytes(this.preamble);
            los.write(pb);
            if (pb.length > 0 && pb[pb.length - 1] != 13 && pb[pb.length - 1] != 10) {
                los.writeln();
            }
        }
        if (this.parts.size() == 0) {
            if (!this.allowEmpty) throw new MessagingException("Empty multipart: " + this.contentType);
            los.writeln(boundary);
            los.writeln();
        } else {
            for (int i = 0; i < this.parts.size(); ++i) {
                los.writeln(boundary);
                ((MimeBodyPart)this.parts.elementAt(i)).writeTo(os);
                los.writeln();
            }
        }
        los.writeln(boundary + "--");
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected synchronized void parse() throws MessagingException {
        block64: {
            block63: {
                if (this.parsed) {
                    return;
                }
                this.initializeProperties();
                in = null;
                sin = null;
                start = 0L;
                end = 0L;
                try {
                    in = this.ds.getInputStream();
                    if (!(in instanceof ByteArrayInputStream || in instanceof BufferedInputStream || in instanceof SharedInputStream)) {
                        in = new BufferedInputStream(in);
                    }
                }
                catch (Exception ex) {
                    throw new MessagingException("No inputstream from datasource", ex);
                }
                if (in instanceof SharedInputStream) {
                    sin = (SharedInputStream)in;
                }
                cType = new ContentType(this.contentType);
                boundary = null;
                if (!this.ignoreExistingBoundaryParameter && (bp = cType.getParameter("boundary")) != null) {
                    boundary = "--" + bp;
                }
                if (boundary == null && !this.ignoreMissingBoundaryParameter && !this.ignoreExistingBoundaryParameter) {
                    throw new MessagingException("Missing boundary parameter");
                }
                try {
                    try {
                        lin = new LineInputStream(in);
                        preamblesb = null;
                        lineSeparator = null;
                        while ((line = lin.readLine()) != null) {
                            for (i = line.length() - 1; i >= 0 && ((c = line.charAt(i)) == ' ' || c == '\t'); --i) {
                            }
                            line = line.substring(0, i + 1);
                            if (boundary != null) {
                                if (line.equals(boundary)) break;
                                if (line.length() == boundary.length() + 2 && line.startsWith(boundary) && line.endsWith("--")) {
                                    line = null;
                                    break;
                                }
                            } else if (line.length() > 2 && line.startsWith("--") && (line.length() <= 4 || !MimeMultipart.allDashes(line))) {
                                boundary = line;
                                break;
                            }
                            if (line.length() <= 0) continue;
                            if (lineSeparator == null) {
                                try {
                                    lineSeparator = System.getProperty("line.separator", "\n");
                                }
                                catch (SecurityException ex) {
                                    lineSeparator = "\n";
                                }
                            }
                            if (preamblesb == null) {
                                preamblesb = new StringBuffer(line.length() + 2);
                            }
                            preamblesb.append(line).append(lineSeparator);
                        }
                        if (preamblesb != null) {
                            this.preamble = preamblesb.toString();
                        }
                        if (line == null) {
                            if (this.allowEmpty == false) throw new MessagingException("Missing start boundary");
                            var31_17 = null;
                            break block63;
                        }
                        bndbytes = ASCIIUtility.getBytes(boundary);
                        bl = bndbytes.length;
                        bcs = new int[256];
                        for (i = 0; i < bl; ++i) {
                            bcs[bndbytes[i] & 255] = i + 1;
                        }
                        gss = new int[bl];
                        block15: for (i = bl; i > 0; --i) {
                            for (j = bl - 1; j >= i; --j) {
                                if (bndbytes[j] != bndbytes[j - i]) continue block15;
                                gss[j - 1] = i;
                            }
                            while (j > 0) {
                                gss[--j] = i;
                            }
                        }
                        gss[bl - 1] = 1;
                        done = false;
                        while (!done) {
                            headers = null;
                            if (sin != null) {
                                start = sin.getPosition();
                                while ((line = lin.readLine()) != null && line.length() > 0) {
                                }
                                if (line == null) {
                                    if (!this.ignoreMissingEndBoundary) {
                                        throw new MessagingException("missing multipart end boundary");
                                    }
                                    this.complete = false;
                                    break block64;
                                }
                            } else {
                                headers = this.createInternetHeaders(in);
                            }
                            if (!in.markSupported()) {
                                throw new MessagingException("Stream doesn't support mark");
                            }
                            buf = null;
                            if (sin == null) {
                                buf = new ByteArrayOutputStream();
                            } else {
                                end = sin.getPosition();
                            }
                            inbuf = new byte[bl];
                            previnbuf = new byte[bl];
                            inSize = 0;
                            prevSize = 0;
                            first = true;
                            while (true) {
                                in.mark(bl + 4 + 1000);
                                eolLen = 0;
                                inSize = MimeMultipart.readFully(in, inbuf, 0, bl);
                                if (inSize < bl) {
                                    if (!this.ignoreMissingEndBoundary) {
                                        throw new MessagingException("missing multipart end boundary");
                                    }
                                    if (sin != null) {
                                        end = sin.getPosition();
                                    }
                                    this.complete = false;
                                    done = true;
                                    break;
                                }
                                for (i = bl - 1; i >= 0 && inbuf[i] == bndbytes[i]; --i) {
                                }
                                if (i < 0) {
                                    eolLen = 0;
                                    if (!(first || (b = previnbuf[prevSize - 1]) != 13 && b != 10)) {
                                        eolLen = 1;
                                        if (b == 10 && prevSize >= 2 && (b = previnbuf[prevSize - 2]) == 13) {
                                            eolLen = 2;
                                        }
                                    }
                                    if (first || eolLen > 0) {
                                        if (sin != null) {
                                            end = sin.getPosition() - (long)bl - (long)eolLen;
                                        }
                                        if ((b2 = in.read()) == 45 && in.read() == 45) {
                                            this.complete = true;
                                            done = true;
                                            break;
                                        }
                                        while (b2 == 32 || b2 == 9) {
                                            b2 = in.read();
                                        }
                                        if (b2 == 10) break;
                                        if (b2 == 13) {
                                            in.mark(1);
                                            if (in.read() == 10) break;
                                            in.reset();
                                            break;
                                        }
                                    }
                                    i = 0;
                                }
                                if ((skip = Math.max(i + 1 - bcs[inbuf[i] & 127], gss[i])) < 2) {
                                    if (sin == null && prevSize > 1) {
                                        buf.write(previnbuf, 0, prevSize - 1);
                                    }
                                    in.reset();
                                    this.skipFully(in, 1L);
                                    if (prevSize >= 1) {
                                        previnbuf[0] = previnbuf[prevSize - 1];
                                        previnbuf[1] = inbuf[0];
                                        prevSize = 2;
                                    } else {
                                        previnbuf[0] = inbuf[0];
                                        prevSize = 1;
                                    }
                                } else {
                                    if (prevSize > 0 && sin == null) {
                                        buf.write(previnbuf, 0, prevSize);
                                    }
                                    prevSize = skip;
                                    in.reset();
                                    this.skipFully(in, prevSize);
                                    tmp = inbuf;
                                    inbuf = previnbuf;
                                    previnbuf = tmp;
                                }
                                first = false;
                            }
                            if (sin != null) {
                                part = this.createMimeBodyPartIs(sin.newStream(start, end));
                            } else {
                                if (prevSize - eolLen > 0) {
                                    buf.write(previnbuf, 0, prevSize - eolLen);
                                }
                                if (!this.complete && inSize > 0) {
                                    buf.write(inbuf, 0, inSize);
                                }
                                part = this.createMimeBodyPart(headers, buf.toByteArray());
                            }
                            super.addBodyPart(part);
                        }
                        break block64;
                    }
                    catch (IOException ioex) {
                        throw new MessagingException("IO Error", ioex);
                    }
                }
                catch (Throwable var30_42) {
                    var31_19 = null;
                    try {
                        in.close();
                        throw var30_42;
                    }
                    catch (IOException var32_22) {
                        // empty catch block
                    }
                    throw var30_42;
                }
            }
            ** try [egrp 4[TRYBLOCK] [7 : 1347->1354)] { 
lbl188:
            // 1 sources

            in.close();
            return;
lbl190:
            // 1 sources

            catch (IOException var32_20) {
                // empty catch block
            }
            return;
        }
        var31_18 = null;
        try {}
        catch (IOException var32_21) {}
        in.close();
        this.parsed = true;
    }

    private static boolean allDashes(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == '-') continue;
            return false;
        }
        return true;
    }

    private static int readFully(InputStream in, byte[] buf, int off, int len) throws IOException {
        int bsize;
        if (len == 0) {
            return 0;
        }
        int total = 0;
        while (len > 0 && (bsize = in.read(buf, off, len)) > 0) {
            off += bsize;
            total += bsize;
            len -= bsize;
        }
        return total > 0 ? total : -1;
    }

    private void skipFully(InputStream in, long offset) throws IOException {
        while (offset > 0L) {
            long cur = in.skip(offset);
            if (cur <= 0L) {
                throw new EOFException("can't skip");
            }
            offset -= cur;
        }
    }

    protected InternetHeaders createInternetHeaders(InputStream is) throws MessagingException {
        return new InternetHeaders(is);
    }

    protected MimeBodyPart createMimeBodyPart(InternetHeaders headers, byte[] content) throws MessagingException {
        return new MimeBodyPart(headers, content);
    }

    protected MimeBodyPart createMimeBodyPart(InputStream is) throws MessagingException {
        return new MimeBodyPart(is);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private MimeBodyPart createMimeBodyPartIs(InputStream is) throws MessagingException {
        MimeBodyPart mimeBodyPart;
        try {
            mimeBodyPart = this.createMimeBodyPart(is);
            Object var4_3 = null;
        }
        catch (Throwable throwable) {
            Object var4_4 = null;
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
        return mimeBodyPart;
    }
}

