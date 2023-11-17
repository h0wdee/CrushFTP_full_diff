/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.internet;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.PropUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.Header;
import javax.mail.MessagingException;

public class InternetHeaders {
    private static final boolean ignoreWhitespaceLines = PropUtil.getBooleanSystemProperty("mail.mime.ignorewhitespacelines", false);
    protected List headers = new ArrayList(40);

    public InternetHeaders() {
        this.headers.add(new InternetHeader("Return-Path", null));
        this.headers.add(new InternetHeader("Received", null));
        this.headers.add(new InternetHeader("Resent-Date", null));
        this.headers.add(new InternetHeader("Resent-From", null));
        this.headers.add(new InternetHeader("Resent-Sender", null));
        this.headers.add(new InternetHeader("Resent-To", null));
        this.headers.add(new InternetHeader("Resent-Cc", null));
        this.headers.add(new InternetHeader("Resent-Bcc", null));
        this.headers.add(new InternetHeader("Resent-Message-Id", null));
        this.headers.add(new InternetHeader("Date", null));
        this.headers.add(new InternetHeader("From", null));
        this.headers.add(new InternetHeader("Sender", null));
        this.headers.add(new InternetHeader("Reply-To", null));
        this.headers.add(new InternetHeader("To", null));
        this.headers.add(new InternetHeader("Cc", null));
        this.headers.add(new InternetHeader("Bcc", null));
        this.headers.add(new InternetHeader("Message-Id", null));
        this.headers.add(new InternetHeader("In-Reply-To", null));
        this.headers.add(new InternetHeader("References", null));
        this.headers.add(new InternetHeader("Subject", null));
        this.headers.add(new InternetHeader("Comments", null));
        this.headers.add(new InternetHeader("Keywords", null));
        this.headers.add(new InternetHeader("Errors-To", null));
        this.headers.add(new InternetHeader("MIME-Version", null));
        this.headers.add(new InternetHeader("Content-Type", null));
        this.headers.add(new InternetHeader("Content-Transfer-Encoding", null));
        this.headers.add(new InternetHeader("Content-MD5", null));
        this.headers.add(new InternetHeader(":", null));
        this.headers.add(new InternetHeader("Content-Length", null));
        this.headers.add(new InternetHeader("Status", null));
    }

    public InternetHeaders(InputStream is) throws MessagingException {
        this.load(is);
    }

    public void load(InputStream is) throws MessagingException {
        LineInputStream lis = new LineInputStream(is);
        String prevline = null;
        StringBuffer lineBuffer = new StringBuffer();
        try {
            String line;
            boolean first = true;
            do {
                if ((line = lis.readLine()) != null && (line.startsWith(" ") || line.startsWith("\t"))) {
                    if (prevline != null) {
                        lineBuffer.append(prevline);
                        prevline = null;
                    }
                    if (first) {
                        String lt = line.trim();
                        if (lt.length() > 0) {
                            lineBuffer.append(lt);
                        }
                    } else {
                        if (lineBuffer.length() > 0) {
                            lineBuffer.append("\r\n");
                        }
                        lineBuffer.append(line);
                    }
                } else {
                    if (prevline != null) {
                        this.addHeaderLine(prevline);
                    } else if (lineBuffer.length() > 0) {
                        this.addHeaderLine(lineBuffer.toString());
                        lineBuffer.setLength(0);
                    }
                    prevline = line;
                }
                first = false;
            } while (line != null && !InternetHeaders.isEmpty(line));
        }
        catch (IOException ioex) {
            throw new MessagingException("Error in input stream", ioex);
        }
    }

    private static final boolean isEmpty(String line) {
        return line.length() == 0 || ignoreWhitespaceLines && line.trim().length() == 0;
    }

    public String[] getHeader(String name) {
        Iterator e = this.headers.iterator();
        ArrayList<String> v = new ArrayList<String>();
        while (e.hasNext()) {
            InternetHeader h = (InternetHeader)e.next();
            if (!name.equalsIgnoreCase(h.getName()) || h.line == null) continue;
            v.add(h.getValue());
        }
        if (v.size() == 0) {
            return null;
        }
        String[] r = new String[v.size()];
        r = v.toArray(r);
        return r;
    }

    public String getHeader(String name, String delimiter) {
        String[] s = this.getHeader(name);
        if (s == null) {
            return null;
        }
        if (s.length == 1 || delimiter == null) {
            return s[0];
        }
        StringBuffer r = new StringBuffer(s[0]);
        for (int i = 1; i < s.length; ++i) {
            r.append(delimiter);
            r.append(s[i]);
        }
        return r.toString();
    }

    public void setHeader(String name, String value) {
        boolean found = false;
        for (int i = 0; i < this.headers.size(); ++i) {
            InternetHeader h = (InternetHeader)this.headers.get(i);
            if (!name.equalsIgnoreCase(h.getName())) continue;
            if (!found) {
                int j;
                h.line = h.line != null && (j = h.line.indexOf(58)) >= 0 ? h.line.substring(0, j + 1) + " " + value : name + ": " + value;
                found = true;
                continue;
            }
            this.headers.remove(i);
            --i;
        }
        if (!found) {
            this.addHeader(name, value);
        }
    }

    public void addHeader(String name, String value) {
        boolean addReverse;
        int pos = this.headers.size();
        boolean bl = addReverse = name.equalsIgnoreCase("Received") || name.equalsIgnoreCase("Return-Path");
        if (addReverse) {
            pos = 0;
        }
        for (int i = this.headers.size() - 1; i >= 0; --i) {
            InternetHeader h = (InternetHeader)this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                if (addReverse) {
                    pos = i;
                } else {
                    this.headers.add(i + 1, new InternetHeader(name, value));
                    return;
                }
            }
            if (addReverse || !h.getName().equals(":")) continue;
            pos = i;
        }
        this.headers.add(pos, new InternetHeader(name, value));
    }

    public void removeHeader(String name) {
        for (int i = 0; i < this.headers.size(); ++i) {
            InternetHeader h = (InternetHeader)this.headers.get(i);
            if (!name.equalsIgnoreCase(h.getName())) continue;
            h.line = null;
        }
    }

    public Enumeration getAllHeaders() {
        return new MatchHeaderEnum(this.headers, null, false);
    }

    public Enumeration getMatchingHeaders(String[] names) {
        return new MatchHeaderEnum(this.headers, names, true);
    }

    public Enumeration getNonMatchingHeaders(String[] names) {
        return new MatchHeaderEnum(this.headers, names, false);
    }

    public void addHeaderLine(String line) {
        try {
            char c = line.charAt(0);
            if (c == ' ' || c == '\t') {
                InternetHeader h = (InternetHeader)this.headers.get(this.headers.size() - 1);
                h.line = h.line + "\r\n" + line;
            } else {
                this.headers.add(new InternetHeader(line));
            }
        }
        catch (StringIndexOutOfBoundsException e) {
            return;
        }
        catch (NoSuchElementException noSuchElementException) {
            // empty catch block
        }
    }

    public Enumeration getAllHeaderLines() {
        return this.getNonMatchingHeaderLines(null);
    }

    public Enumeration getMatchingHeaderLines(String[] names) {
        return new MatchStringEnum(this.headers, names, true);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) {
        return new MatchStringEnum(this.headers, names, false);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class MatchHeaderEnum
    extends MatchEnum
    implements Enumeration<Header> {
        MatchHeaderEnum(List<InternetHeader> v, String[] n, boolean m) {
            super(v, n, m, false);
        }

        @Override
        public Header nextElement() {
            return (Header)super.nextElement();
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class MatchStringEnum
    extends MatchEnum
    implements Enumeration<String> {
        MatchStringEnum(List<InternetHeader> v, String[] n, boolean m) {
            super(v, n, m, true);
        }

        @Override
        public String nextElement() {
            return (String)super.nextElement();
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class MatchEnum {
        private Iterator<InternetHeader> e;
        private String[] names;
        private boolean match;
        private boolean want_line;
        private InternetHeader next_header;

        MatchEnum(List<InternetHeader> v, String[] n, boolean m, boolean l) {
            this.e = v.iterator();
            this.names = n;
            this.match = m;
            this.want_line = l;
            this.next_header = null;
        }

        public boolean hasMoreElements() {
            if (this.next_header == null) {
                this.next_header = this.nextMatch();
            }
            return this.next_header != null;
        }

        public Object nextElement() {
            if (this.next_header == null) {
                this.next_header = this.nextMatch();
            }
            if (this.next_header == null) {
                throw new NoSuchElementException("No more headers");
            }
            InternetHeader h = this.next_header;
            this.next_header = null;
            if (this.want_line) {
                return h.line;
            }
            return new Header(h.getName(), h.getValue());
        }

        private InternetHeader nextMatch() {
            block0: while (this.e.hasNext()) {
                InternetHeader h = this.e.next();
                if (h.line == null) continue;
                if (this.names == null) {
                    return this.match ? null : h;
                }
                for (int i = 0; i < this.names.length; ++i) {
                    if (!this.names[i].equalsIgnoreCase(h.getName())) continue;
                    if (!this.match) continue block0;
                    return h;
                }
                if (this.match) continue;
                return h;
            }
            return null;
        }
    }

    protected static final class InternetHeader
    extends Header {
        String line;

        public InternetHeader(String l) {
            super("", "");
            int i = l.indexOf(58);
            this.name = i < 0 ? l.trim() : l.substring(0, i).trim();
            this.line = l;
        }

        public InternetHeader(String n, String v) {
            super(n, "");
            this.line = v != null ? n + ": " + v : null;
        }

        public String getValue() {
            char c;
            int j;
            int i = this.line.indexOf(58);
            if (i < 0) {
                return this.line;
            }
            for (j = i + 1; j < this.line.length() && ((c = this.line.charAt(j)) == ' ' || c == '\t' || c == '\r' || c == '\n'); ++j) {
            }
            return this.line.substring(j);
        }
    }
}

