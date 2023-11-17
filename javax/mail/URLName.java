/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Locale;

public class URLName {
    protected String fullURL;
    private String protocol;
    private String username;
    private String password;
    private String host;
    private InetAddress hostAddress;
    private boolean hostAddressKnown = false;
    private int port = -1;
    private String file;
    private String ref;
    private int hashCode = 0;
    private static boolean doEncode;
    static BitSet dontNeedEncoding;
    static final int caseDiff = 32;

    public URLName(String protocol, String host, int port, String file, String username, String password) {
        int refStart;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        if (file != null && (refStart = file.indexOf(35)) != -1) {
            this.file = file.substring(0, refStart);
            this.ref = file.substring(refStart + 1);
        } else {
            this.file = file;
            this.ref = null;
        }
        this.username = doEncode ? URLName.encode(username) : username;
        this.password = doEncode ? URLName.encode(password) : password;
    }

    public URLName(URL url) {
        this(url.toString());
    }

    public URLName(String url) {
        this.parseString(url);
    }

    public String toString() {
        if (this.fullURL == null) {
            StringBuffer tempURL = new StringBuffer();
            if (this.protocol != null) {
                tempURL.append(this.protocol);
                tempURL.append(":");
            }
            if (this.username != null || this.host != null) {
                tempURL.append("//");
                if (this.username != null) {
                    tempURL.append(this.username);
                    if (this.password != null) {
                        tempURL.append(":");
                        tempURL.append(this.password);
                    }
                    tempURL.append("@");
                }
                if (this.host != null) {
                    tempURL.append(this.host);
                }
                if (this.port != -1) {
                    tempURL.append(":");
                    tempURL.append(Integer.toString(this.port));
                }
                if (this.file != null) {
                    tempURL.append("/");
                }
            }
            if (this.file != null) {
                tempURL.append(this.file);
            }
            if (this.ref != null) {
                tempURL.append("#");
                tempURL.append(this.ref);
            }
            this.fullURL = tempURL.toString();
        }
        return this.fullURL;
    }

    protected void parseString(String url) {
        int refStart;
        this.password = null;
        this.username = null;
        this.host = null;
        this.ref = null;
        this.file = null;
        this.protocol = null;
        this.port = -1;
        int len = url.length();
        int protocolEnd = url.indexOf(58);
        if (protocolEnd != -1) {
            this.protocol = url.substring(0, protocolEnd);
        }
        if (url.regionMatches(protocolEnd + 1, "//", 0, 2)) {
            int portindex;
            String fullhost = null;
            int fileStart = url.indexOf(47, protocolEnd + 3);
            if (fileStart != -1) {
                fullhost = url.substring(protocolEnd + 3, fileStart);
                this.file = fileStart + 1 < len ? url.substring(fileStart + 1) : "";
            } else {
                fullhost = url.substring(protocolEnd + 3);
            }
            int i = fullhost.indexOf(64);
            if (i != -1) {
                String fulluserpass = fullhost.substring(0, i);
                fullhost = fullhost.substring(i + 1);
                int passindex = fulluserpass.indexOf(58);
                if (passindex != -1) {
                    this.username = fulluserpass.substring(0, passindex);
                    this.password = fulluserpass.substring(passindex + 1);
                } else {
                    this.username = fulluserpass;
                }
            }
            if ((portindex = fullhost.length() > 0 && fullhost.charAt(0) == '[' ? fullhost.indexOf(58, fullhost.indexOf(93)) : fullhost.indexOf(58)) != -1) {
                String portstring = fullhost.substring(portindex + 1);
                if (portstring.length() > 0) {
                    try {
                        this.port = Integer.parseInt(portstring);
                    }
                    catch (NumberFormatException nfex) {
                        this.port = -1;
                    }
                }
                this.host = fullhost.substring(0, portindex);
            } else {
                this.host = fullhost;
            }
        } else if (protocolEnd + 1 < len) {
            this.file = url.substring(protocolEnd + 1);
        }
        if (this.file != null && (refStart = this.file.indexOf(35)) != -1) {
            this.ref = this.file.substring(refStart + 1);
            this.file = this.file.substring(0, refStart);
        }
    }

    public int getPort() {
        return this.port;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getFile() {
        return this.file;
    }

    public String getRef() {
        return this.ref;
    }

    public String getHost() {
        return this.host;
    }

    public String getUsername() {
        return doEncode ? URLName.decode(this.username) : this.username;
    }

    public String getPassword() {
        return doEncode ? URLName.decode(this.password) : this.password;
    }

    public URL getURL() throws MalformedURLException {
        return new URL(this.getProtocol(), this.getHost(), this.getPort(), this.getFile());
    }

    public boolean equals(Object obj) {
        String f2;
        if (!(obj instanceof URLName)) {
            return false;
        }
        URLName u2 = (URLName)obj;
        if (!(this.protocol == u2.protocol || this.protocol != null && this.protocol.equals(u2.protocol))) {
            return false;
        }
        InetAddress a1 = this.getHostAddress();
        InetAddress a2 = u2.getHostAddress();
        if (a1 != null && a2 != null ? !a1.equals(a2) : (this.host != null && u2.host != null ? !this.host.equalsIgnoreCase(u2.host) : this.host != u2.host)) {
            return false;
        }
        if (!(this.username == u2.username || this.username != null && this.username.equals(u2.username))) {
            return false;
        }
        String f1 = this.file == null ? "" : this.file;
        String string = f2 = u2.file == null ? "" : u2.file;
        if (!f1.equals(f2)) {
            return false;
        }
        return this.port == u2.port;
    }

    public int hashCode() {
        InetAddress addr;
        if (this.hashCode != 0) {
            return this.hashCode;
        }
        if (this.protocol != null) {
            this.hashCode += this.protocol.hashCode();
        }
        if ((addr = this.getHostAddress()) != null) {
            this.hashCode += addr.hashCode();
        } else if (this.host != null) {
            this.hashCode += this.host.toLowerCase(Locale.ENGLISH).hashCode();
        }
        if (this.username != null) {
            this.hashCode += this.username.hashCode();
        }
        if (this.file != null) {
            this.hashCode += this.file.hashCode();
        }
        this.hashCode += this.port;
        return this.hashCode;
    }

    private synchronized InetAddress getHostAddress() {
        if (this.hostAddressKnown) {
            return this.hostAddress;
        }
        if (this.host == null) {
            return null;
        }
        try {
            this.hostAddress = InetAddress.getByName(this.host);
        }
        catch (UnknownHostException ex) {
            this.hostAddress = null;
        }
        this.hostAddressKnown = true;
        return this.hostAddress;
    }

    static String encode(String s) {
        if (s == null) {
            return null;
        }
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c != ' ' && dontNeedEncoding.get(c)) continue;
            return URLName._encode(s);
        }
        return s;
    }

    private static String _encode(String s) {
        int maxBytesPerChar = 10;
        StringBuffer out = new StringBuffer(s.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        OutputStreamWriter writer = new OutputStreamWriter(buf);
        for (int i = 0; i < s.length(); ++i) {
            int c = s.charAt(i);
            if (dontNeedEncoding.get(c)) {
                if (c == 32) {
                    c = 43;
                }
                out.append((char)c);
                continue;
            }
            try {
                writer.write(c);
                writer.flush();
            }
            catch (IOException e) {
                buf.reset();
                continue;
            }
            byte[] ba = buf.toByteArray();
            for (int j = 0; j < ba.length; ++j) {
                out.append('%');
                char ch = Character.forDigit(ba[j] >> 4 & 0xF, 16);
                if (Character.isLetter(ch)) {
                    ch = (char)(ch - 32);
                }
                out.append(ch);
                ch = Character.forDigit(ba[j] & 0xF, 16);
                if (Character.isLetter(ch)) {
                    ch = (char)(ch - 32);
                }
                out.append(ch);
            }
            buf.reset();
        }
        return out.toString();
    }

    static String decode(String s) {
        if (s == null) {
            return null;
        }
        if (URLName.indexOfAny(s, "+%") == -1) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        block8: for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '+': {
                    sb.append(' ');
                    continue block8;
                }
                case '%': {
                    try {
                        sb.append((char)Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    }
                    catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Illegal URL encoded value: " + s.substring(i, i + 3));
                    }
                    i += 2;
                    continue block8;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        String result = sb.toString();
        try {
            byte[] inputBytes = result.getBytes("8859_1");
            result = new String(inputBytes);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            // empty catch block
        }
        return result;
    }

    private static int indexOfAny(String s, String any) {
        return URLName.indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
        try {
            int len = s.length();
            for (int i = start; i < len; ++i) {
                if (any.indexOf(s.charAt(i)) < 0) continue;
                return i;
            }
            return -1;
        }
        catch (StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    static {
        int i;
        doEncode = true;
        try {
            doEncode = !Boolean.getBoolean("mail.URLName.dontencode");
        }
        catch (Exception exception) {
            // empty catch block
        }
        dontNeedEncoding = new BitSet(256);
        for (i = 97; i <= 122; ++i) {
            dontNeedEncoding.set(i);
        }
        for (i = 65; i <= 90; ++i) {
            dontNeedEncoding.set(i);
        }
        for (i = 48; i <= 57; ++i) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set(32);
        dontNeedEncoding.set(45);
        dontNeedEncoding.set(95);
        dontNeedEncoding.set(46);
        dontNeedEncoding.set(42);
    }
}

