/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 *  javax.activation.DataSource
 */
package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.BEncoderStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.QDecoderStream;
import com.sun.mail.util.QEncoderStream;
import com.sun.mail.util.QPDecoderStream;
import com.sun.mail.util.QPEncoderStream;
import com.sun.mail.util.UUDecoderStream;
import com.sun.mail.util.UUEncoderStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.EncodingAware;
import javax.mail.MessagingException;
import javax.mail.internet.AsciiOutputStream;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MimeUtility {
    public static final int ALL = -1;
    private static final Map<String, Boolean> nonAsciiCharsetMap;
    private static final boolean decodeStrict;
    private static final boolean encodeEolStrict;
    private static final boolean ignoreUnknownEncoding;
    private static final boolean foldEncodedWords;
    private static final boolean foldText;
    private static String defaultJavaCharset;
    private static String defaultMIMECharset;
    private static Map<String, String> mime2java;
    private static Map<String, String> java2mime;
    static final int ALL_ASCII = 1;
    static final int MOSTLY_ASCII = 2;
    static final int MOSTLY_NONASCII = 3;

    private MimeUtility() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String getEncoding(DataSource ds) {
        ContentType cType = null;
        InputStream is = null;
        String encoding = null;
        if (ds instanceof EncodingAware && (encoding = ((EncodingAware)ds).getEncoding()) != null) {
            return encoding;
        }
        try {
            cType = new ContentType(ds.getContentType());
            is = ds.getInputStream();
            boolean isText = cType.match("text/*");
            int i = MimeUtility.checkAscii(is, -1, !isText);
            switch (i) {
                case 1: {
                    encoding = "7bit";
                    return encoding;
                }
                case 2: {
                    if (isText && MimeUtility.nonAsciiCharset(cType)) {
                        encoding = "base64";
                        return encoding;
                    } else {
                        encoding = "quoted-printable";
                        return encoding;
                    }
                }
                default: {
                    encoding = "base64";
                    return encoding;
                }
            }
        }
        catch (Exception ex) {
            String string = "base64";
            return string;
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean nonAsciiCharset(ContentType ct) {
        Boolean bool;
        String charset = ct.getParameter("charset");
        if (charset == null) {
            return false;
        }
        charset = charset.toLowerCase(Locale.ENGLISH);
        Map<String, Boolean> map = nonAsciiCharsetMap;
        synchronized (map) {
            bool = nonAsciiCharsetMap.get(charset);
        }
        if (bool == null) {
            try {
                byte[] b = "\r\n".getBytes(charset);
                bool = b.length != 2 || b[0] != 13 || b[1] != 10;
            }
            catch (UnsupportedEncodingException uex) {
                bool = Boolean.FALSE;
            }
            catch (RuntimeException ex) {
                bool = Boolean.TRUE;
            }
            map = nonAsciiCharsetMap;
            synchronized (map) {
                nonAsciiCharsetMap.put(charset, bool);
            }
        }
        return bool;
    }

    public static String getEncoding(DataHandler dh) {
        ContentType cType = null;
        String encoding = null;
        if (dh.getName() != null) {
            return MimeUtility.getEncoding(dh.getDataSource());
        }
        try {
            cType = new ContentType(dh.getContentType());
        }
        catch (Exception ex) {
            return "base64";
        }
        if (cType.match("text/*")) {
            AsciiOutputStream aos = new AsciiOutputStream(false, false);
            try {
                dh.writeTo((OutputStream)aos);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            switch (aos.getAscii()) {
                case 1: {
                    encoding = "7bit";
                    break;
                }
                case 2: {
                    encoding = "quoted-printable";
                    break;
                }
                default: {
                    encoding = "base64";
                    break;
                }
            }
        } else {
            AsciiOutputStream aos = new AsciiOutputStream(true, encodeEolStrict);
            try {
                dh.writeTo((OutputStream)aos);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            encoding = aos.getAscii() == 1 ? "7bit" : "base64";
        }
        return encoding;
    }

    public static InputStream decode(InputStream is, String encoding) throws MessagingException {
        if (encoding.equalsIgnoreCase("base64")) {
            return new BASE64DecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPDecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUDecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return is;
        }
        if (!ignoreUnknownEncoding) {
            throw new MessagingException("Unknown encoding: " + encoding);
        }
        return is;
    }

    public static OutputStream encode(OutputStream os, String encoding) throws MessagingException {
        if (encoding == null) {
            return os;
        }
        if (encoding.equalsIgnoreCase("base64")) {
            return new BASE64EncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return os;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static OutputStream encode(OutputStream os, String encoding, String filename) throws MessagingException {
        if (encoding == null) {
            return os;
        }
        if (encoding.equalsIgnoreCase("base64")) {
            return new BASE64EncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUEncoderStream(os, filename);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return os;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static String encodeText(String text) throws UnsupportedEncodingException {
        return MimeUtility.encodeText(text, null, null);
    }

    public static String encodeText(String text, String charset, String encoding) throws UnsupportedEncodingException {
        return MimeUtility.encodeWord(text, charset, encoding, false);
    }

    public static String decodeText(String etext) throws UnsupportedEncodingException {
        String lwsp = " \t\n\r";
        if (etext.indexOf("=?") == -1) {
            return etext;
        }
        StringTokenizer st = new StringTokenizer(etext, lwsp, true);
        StringBuffer sb = new StringBuffer();
        StringBuffer wsb = new StringBuffer();
        boolean prevWasEncoded = false;
        while (st.hasMoreTokens()) {
            String word;
            String s = st.nextToken();
            char c = s.charAt(0);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                wsb.append(c);
                continue;
            }
            try {
                word = MimeUtility.decodeWord(s);
                if (!prevWasEncoded && wsb.length() > 0) {
                    sb.append(wsb);
                }
                prevWasEncoded = true;
            }
            catch (ParseException pex) {
                word = s;
                if (!decodeStrict) {
                    String dword = MimeUtility.decodeInnerWords(word);
                    if (dword != word) {
                        if (!(prevWasEncoded && word.startsWith("=?") || wsb.length() <= 0)) {
                            sb.append(wsb);
                        }
                        prevWasEncoded = word.endsWith("?=");
                        word = dword;
                    } else {
                        if (wsb.length() > 0) {
                            sb.append(wsb);
                        }
                        prevWasEncoded = false;
                    }
                }
                if (wsb.length() > 0) {
                    sb.append(wsb);
                }
                prevWasEncoded = false;
            }
            sb.append(word);
            wsb.setLength(0);
        }
        sb.append(wsb);
        return sb.toString();
    }

    public static String encodeWord(String word) throws UnsupportedEncodingException {
        return MimeUtility.encodeWord(word, null, null);
    }

    public static String encodeWord(String word, String charset, String encoding) throws UnsupportedEncodingException {
        return MimeUtility.encodeWord(word, charset, encoding, true);
    }

    private static String encodeWord(String string, String charset, String encoding, boolean encodingWord) throws UnsupportedEncodingException {
        boolean b64;
        String jcharset;
        int ascii = MimeUtility.checkAscii(string);
        if (ascii == 1) {
            return string;
        }
        if (charset == null) {
            jcharset = MimeUtility.getDefaultJavaCharset();
            charset = MimeUtility.getDefaultMIMECharset();
        } else {
            jcharset = MimeUtility.javaCharset(charset);
        }
        if (encoding == null) {
            encoding = ascii != 3 ? "Q" : "B";
        }
        if (encoding.equalsIgnoreCase("B")) {
            b64 = true;
        } else if (encoding.equalsIgnoreCase("Q")) {
            b64 = false;
        } else {
            throw new UnsupportedEncodingException("Unknown transfer encoding: " + encoding);
        }
        StringBuffer outb = new StringBuffer();
        MimeUtility.doEncode(string, b64, jcharset, 68 - charset.length(), "=?" + charset + "?" + encoding + "?", true, encodingWord, outb);
        return outb.toString();
    }

    private static void doEncode(String string, boolean b64, String jcharset, int avail, String prefix, boolean first, boolean encodingWord, StringBuffer buf) throws UnsupportedEncodingException {
        int size;
        byte[] bytes = string.getBytes(jcharset);
        int len = b64 ? BEncoderStream.encodedLength(bytes) : QEncoderStream.encodedLength(bytes, encodingWord);
        if (len > avail && (size = string.length()) > 1) {
            int split = size / 2;
            if (Character.isHighSurrogate(string.charAt(split - 1))) {
                --split;
            }
            if (split > 0) {
                MimeUtility.doEncode(string.substring(0, split), b64, jcharset, avail, prefix, first, encodingWord, buf);
            }
            MimeUtility.doEncode(string.substring(split, size), b64, jcharset, avail, prefix, false, encodingWord, buf);
        } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FilterOutputStream eos = b64 ? new BEncoderStream(os) : new QEncoderStream((OutputStream)os, encodingWord);
            try {
                ((OutputStream)eos).write(bytes);
                ((OutputStream)eos).close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            byte[] encodedBytes = os.toByteArray();
            if (!first) {
                if (foldEncodedWords) {
                    buf.append("\r\n ");
                } else {
                    buf.append(" ");
                }
            }
            buf.append(prefix);
            for (int i = 0; i < encodedBytes.length; ++i) {
                buf.append((char)encodedBytes[i]);
            }
            buf.append("?=");
        }
    }

    public static String decodeWord(String eword) throws ParseException, UnsupportedEncodingException {
        if (!eword.startsWith("=?")) {
            throw new ParseException("encoded word does not start with \"=?\": " + eword);
        }
        int start = 2;
        int pos = eword.indexOf(63, start);
        if (pos == -1) {
            throw new ParseException("encoded word does not include charset: " + eword);
        }
        String charset = eword.substring(start, pos);
        int lpos = charset.indexOf(42);
        if (lpos >= 0) {
            charset = charset.substring(0, lpos);
        }
        charset = MimeUtility.javaCharset(charset);
        start = pos + 1;
        if ((pos = eword.indexOf(63, start)) == -1) {
            throw new ParseException("encoded word does not include encoding: " + eword);
        }
        String encoding = eword.substring(start, pos);
        start = pos + 1;
        if ((pos = eword.indexOf("?=", start)) == -1) {
            throw new ParseException("encoded word does not end with \"?=\": " + eword);
        }
        String word = eword.substring(start, pos);
        try {
            String decodedWord;
            if (word.length() > 0) {
                FilterInputStream is;
                ByteArrayInputStream bis = new ByteArrayInputStream(ASCIIUtility.getBytes(word));
                if (encoding.equalsIgnoreCase("B")) {
                    is = new BASE64DecoderStream(bis);
                } else if (encoding.equalsIgnoreCase("Q")) {
                    is = new QDecoderStream(bis);
                } else {
                    throw new UnsupportedEncodingException("unknown encoding: " + encoding);
                }
                int count = bis.available();
                byte[] bytes = new byte[count];
                count = ((InputStream)is).read(bytes, 0, count);
                decodedWord = count <= 0 ? "" : new String(bytes, 0, count, charset);
            } else {
                decodedWord = "";
            }
            if (pos + 2 < eword.length()) {
                String rest = eword.substring(pos + 2);
                if (!decodeStrict) {
                    rest = MimeUtility.decodeInnerWords(rest);
                }
                decodedWord = decodedWord + rest;
            }
            return decodedWord;
        }
        catch (UnsupportedEncodingException uex) {
            throw uex;
        }
        catch (IOException ioex) {
            throw new ParseException(ioex.toString());
        }
        catch (IllegalArgumentException iex) {
            throw new UnsupportedEncodingException(charset);
        }
    }

    private static String decodeInnerWords(String word) throws UnsupportedEncodingException {
        int i;
        int start = 0;
        StringBuffer buf = new StringBuffer();
        while ((i = word.indexOf("=?", start)) >= 0) {
            buf.append(word.substring(start, i));
            int end = word.indexOf(63, i + 2);
            if (end < 0 || (end = word.indexOf(63, end + 1)) < 0 || (end = word.indexOf("?=", end + 1)) < 0) break;
            String s = word.substring(i, end + 2);
            try {
                s = MimeUtility.decodeWord(s);
            }
            catch (ParseException parseException) {
                // empty catch block
            }
            buf.append(s);
            start = end + 2;
        }
        if (start == 0) {
            return word;
        }
        if (start < word.length()) {
            buf.append(word.substring(start));
        }
        return buf.toString();
    }

    public static String quote(String word, String specials) {
        int len;
        int n = len = word == null ? 0 : word.length();
        if (len == 0) {
            return "\"\"";
        }
        boolean needQuoting = false;
        for (int i = 0; i < len; ++i) {
            char c = word.charAt(i);
            if (c == '\"' || c == '\\' || c == '\r' || c == '\n') {
                StringBuffer sb = new StringBuffer(len + 3);
                sb.append('\"');
                sb.append(word.substring(0, i));
                int lastc = 0;
                for (int j = i; j < len; ++j) {
                    char cc = word.charAt(j);
                    if (!(cc != '\"' && cc != '\\' && cc != '\r' && cc != '\n' || cc == '\n' && lastc == 13)) {
                        sb.append('\\');
                    }
                    sb.append(cc);
                    lastc = cc;
                }
                sb.append('\"');
                return sb.toString();
            }
            if (c >= ' ' && c < '\u007f' && specials.indexOf(c) < 0) continue;
            needQuoting = true;
        }
        if (needQuoting) {
            StringBuffer sb = new StringBuffer(len + 2);
            sb.append('\"').append(word).append('\"');
            return sb.toString();
        }
        return word;
    }

    public static String fold(int used, String s) {
        char c;
        int end;
        if (!foldText) {
            return s;
        }
        for (end = s.length() - 1; end >= 0 && ((c = s.charAt(end)) == ' ' || c == '\t' || c == '\r' || c == '\n'); --end) {
        }
        if (end != s.length() - 1) {
            s = s.substring(0, end + 1);
        }
        if (used + s.length() <= 76) {
            return MimeUtility.makesafe(s);
        }
        StringBuilder sb = new StringBuilder(s.length() + 4);
        char lastc = '\u0000';
        while (used + s.length() > 76) {
            int lastspace = -1;
            for (int i = 0; i < s.length() && (lastspace == -1 || used + i <= 76); ++i) {
                c = s.charAt(i);
                if ((c == ' ' || c == '\t') && lastc != ' ' && lastc != '\t') {
                    lastspace = i;
                }
                lastc = c;
            }
            if (lastspace == -1) {
                sb.append(s);
                s = "";
                used = 0;
                break;
            }
            sb.append(s.substring(0, lastspace));
            sb.append("\r\n");
            lastc = s.charAt(lastspace);
            sb.append(lastc);
            s = s.substring(lastspace + 1);
            used = 1;
        }
        sb.append(s);
        return MimeUtility.makesafe(sb);
    }

    private static String makesafe(CharSequence s) {
        char c;
        int i;
        for (i = 0; i < s.length() && (c = s.charAt(i)) != '\r' && c != '\n'; ++i) {
        }
        if (i == s.length()) {
            return s.toString();
        }
        StringBuilder sb = new StringBuilder(s.length() + 1);
        BufferedReader r = new BufferedReader(new StringReader(s.toString()));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().length() == 0) continue;
                if (sb.length() > 0) {
                    sb.append("\r\n");
                    assert (line.length() > 0);
                    char c2 = line.charAt(0);
                    if (c2 != ' ' && c2 != '\t') {
                        sb.append(' ');
                    }
                }
                sb.append(line);
            }
        }
        catch (IOException ex) {
            return s.toString();
        }
        return sb.toString();
    }

    public static String unfold(String s) {
        int i;
        if (!foldText) {
            return s;
        }
        StringBuffer sb = null;
        while ((i = MimeUtility.indexOfAny(s, "\r\n")) >= 0) {
            int start = i++;
            int l = s.length();
            if (i < l && s.charAt(i - 1) == '\r' && s.charAt(i) == '\n') {
                ++i;
            }
            if (start == 0 || s.charAt(start - 1) != '\\') {
                char c;
                if (i < l && ((c = s.charAt(i)) == ' ' || c == '\t')) {
                    ++i;
                    while (i < l && ((c = s.charAt(i)) == ' ' || c == '\t')) {
                        ++i;
                    }
                    if (sb == null) {
                        sb = new StringBuffer(s.length());
                    }
                    if (start != 0) {
                        sb.append(s.substring(0, start));
                        sb.append(' ');
                    }
                    s = s.substring(i);
                    continue;
                }
                if (sb == null) {
                    sb = new StringBuffer(s.length());
                }
                sb.append(s.substring(0, i));
                s = s.substring(i);
                continue;
            }
            if (sb == null) {
                sb = new StringBuffer(s.length());
            }
            sb.append(s.substring(0, start - 1));
            sb.append(s.substring(start, i));
            s = s.substring(i);
        }
        if (sb != null) {
            sb.append(s);
            return sb.toString();
        }
        return s;
    }

    private static int indexOfAny(String s, String any) {
        return MimeUtility.indexOfAny(s, any, 0);
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

    public static String javaCharset(String charset) {
        if (mime2java == null || charset == null) {
            return charset;
        }
        String alias = mime2java.get(charset.toLowerCase(Locale.ENGLISH));
        return alias == null ? charset : alias;
    }

    public static String mimeCharset(String charset) {
        if (java2mime == null || charset == null) {
            return charset;
        }
        String alias = java2mime.get(charset.toLowerCase(Locale.ENGLISH));
        return alias == null ? charset : alias;
    }

    public static String getDefaultJavaCharset() {
        block6: {
            if (defaultJavaCharset == null) {
                String mimecs = null;
                try {
                    mimecs = System.getProperty("mail.mime.charset");
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                if (mimecs != null && mimecs.length() > 0) {
                    defaultJavaCharset = MimeUtility.javaCharset(mimecs);
                    return defaultJavaCharset;
                }
                try {
                    defaultJavaCharset = System.getProperty("file.encoding", "8859_1");
                }
                catch (SecurityException sex) {
                    class NullInputStream
                    extends InputStream {
                        NullInputStream() {
                        }

                        public int read() {
                            return 0;
                        }
                    }
                    InputStreamReader reader = new InputStreamReader(new NullInputStream());
                    defaultJavaCharset = reader.getEncoding();
                    if (defaultJavaCharset != null) break block6;
                    defaultJavaCharset = "8859_1";
                }
            }
        }
        return defaultJavaCharset;
    }

    static String getDefaultMIMECharset() {
        if (defaultMIMECharset == null) {
            try {
                defaultMIMECharset = System.getProperty("mail.mime.charset");
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
        }
        if (defaultMIMECharset == null) {
            defaultMIMECharset = MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());
        }
        return defaultMIMECharset;
    }

    private static void loadMappings(LineInputStream is, Map<String, String> table) {
        while (true) {
            String currLine;
            try {
                currLine = is.readLine();
            }
            catch (IOException ioex) {
                break;
            }
            if (currLine == null || currLine.startsWith("--") && currLine.endsWith("--")) break;
            if (currLine.trim().length() == 0 || currLine.startsWith("#")) continue;
            StringTokenizer tk = new StringTokenizer(currLine, " \t");
            try {
                String key = tk.nextToken();
                String value = tk.nextToken();
                table.put(key.toLowerCase(Locale.ENGLISH), value);
            }
            catch (NoSuchElementException noSuchElementException) {}
        }
    }

    static int checkAscii(String s) {
        int ascii = 0;
        int non_ascii = 0;
        int l = s.length();
        for (int i = 0; i < l; ++i) {
            if (MimeUtility.nonascii(s.charAt(i))) {
                ++non_ascii;
                continue;
            }
            ++ascii;
        }
        if (non_ascii == 0) {
            return 1;
        }
        if (ascii > non_ascii) {
            return 2;
        }
        return 3;
    }

    static int checkAscii(byte[] b) {
        int ascii = 0;
        int non_ascii = 0;
        for (int i = 0; i < b.length; ++i) {
            if (MimeUtility.nonascii(b[i] & 0xFF)) {
                ++non_ascii;
                continue;
            }
            ++ascii;
        }
        if (non_ascii == 0) {
            return 1;
        }
        if (ascii > non_ascii) {
            return 2;
        }
        return 3;
    }

    static int checkAscii(InputStream is, int max, boolean breakOnNonAscii) {
        int ascii = 0;
        int non_ascii = 0;
        int block = 4096;
        int linelen = 0;
        boolean longLine = false;
        boolean badEOL = false;
        boolean checkEOL = encodeEolStrict && breakOnNonAscii;
        byte[] buf = null;
        if (max != 0) {
            block = max == -1 ? 4096 : Math.min(max, 4096);
            buf = new byte[block];
        }
        while (max != 0) {
            int len;
            try {
                len = is.read(buf, 0, block);
                if (len == -1) break;
                int lastb = 0;
                for (int i = 0; i < len; ++i) {
                    int b = buf[i] & 0xFF;
                    if (checkEOL && (lastb == 13 && b != 10 || lastb != 13 && b == 10)) {
                        badEOL = true;
                    }
                    if (b == 13 || b == 10) {
                        linelen = 0;
                    } else if (++linelen > 998) {
                        longLine = true;
                    }
                    if (MimeUtility.nonascii(b)) {
                        if (breakOnNonAscii) {
                            return 3;
                        }
                        ++non_ascii;
                    } else {
                        ++ascii;
                    }
                    lastb = b;
                }
            }
            catch (IOException ioex) {
                break;
            }
            if (max == -1) continue;
            max -= len;
        }
        if (max == 0 && breakOnNonAscii) {
            return 3;
        }
        if (non_ascii == 0) {
            if (badEOL) {
                return 3;
            }
            if (longLine) {
                return 2;
            }
            return 1;
        }
        if (ascii > non_ascii) {
            return 2;
        }
        return 3;
    }

    static final boolean nonascii(int b) {
        return b >= 127 || b < 32 && b != 13 && b != 10 && b != 9;
    }

    static {
        block11: {
            nonAsciiCharsetMap = new HashMap<String, Boolean>();
            decodeStrict = PropUtil.getBooleanSystemProperty("mail.mime.decodetext.strict", true);
            encodeEolStrict = PropUtil.getBooleanSystemProperty("mail.mime.encodeeol.strict", false);
            ignoreUnknownEncoding = PropUtil.getBooleanSystemProperty("mail.mime.ignoreunknownencoding", false);
            foldEncodedWords = PropUtil.getBooleanSystemProperty("mail.mime.foldencodedwords", false);
            foldText = PropUtil.getBooleanSystemProperty("mail.mime.foldtext", true);
            java2mime = new HashMap<String, String>(40);
            mime2java = new HashMap<String, String>(10);
            try {
                InputStream is = MimeUtility.class.getResourceAsStream("/META-INF/javamail.charset.map");
                if (is == null) break block11;
                try {
                    is = new LineInputStream(is);
                    MimeUtility.loadMappings((LineInputStream)is, java2mime);
                    MimeUtility.loadMappings((LineInputStream)is, mime2java);
                }
                finally {
                    try {
                        is.close();
                    }
                    catch (Exception exception) {}
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (java2mime.isEmpty()) {
            java2mime.put("8859_1", "ISO-8859-1");
            java2mime.put("iso8859_1", "ISO-8859-1");
            java2mime.put("iso8859-1", "ISO-8859-1");
            java2mime.put("8859_2", "ISO-8859-2");
            java2mime.put("iso8859_2", "ISO-8859-2");
            java2mime.put("iso8859-2", "ISO-8859-2");
            java2mime.put("8859_3", "ISO-8859-3");
            java2mime.put("iso8859_3", "ISO-8859-3");
            java2mime.put("iso8859-3", "ISO-8859-3");
            java2mime.put("8859_4", "ISO-8859-4");
            java2mime.put("iso8859_4", "ISO-8859-4");
            java2mime.put("iso8859-4", "ISO-8859-4");
            java2mime.put("8859_5", "ISO-8859-5");
            java2mime.put("iso8859_5", "ISO-8859-5");
            java2mime.put("iso8859-5", "ISO-8859-5");
            java2mime.put("8859_6", "ISO-8859-6");
            java2mime.put("iso8859_6", "ISO-8859-6");
            java2mime.put("iso8859-6", "ISO-8859-6");
            java2mime.put("8859_7", "ISO-8859-7");
            java2mime.put("iso8859_7", "ISO-8859-7");
            java2mime.put("iso8859-7", "ISO-8859-7");
            java2mime.put("8859_8", "ISO-8859-8");
            java2mime.put("iso8859_8", "ISO-8859-8");
            java2mime.put("iso8859-8", "ISO-8859-8");
            java2mime.put("8859_9", "ISO-8859-9");
            java2mime.put("iso8859_9", "ISO-8859-9");
            java2mime.put("iso8859-9", "ISO-8859-9");
            java2mime.put("sjis", "Shift_JIS");
            java2mime.put("jis", "ISO-2022-JP");
            java2mime.put("iso2022jp", "ISO-2022-JP");
            java2mime.put("euc_jp", "euc-jp");
            java2mime.put("koi8_r", "koi8-r");
            java2mime.put("euc_cn", "euc-cn");
            java2mime.put("euc_tw", "euc-tw");
            java2mime.put("euc_kr", "euc-kr");
        }
        if (mime2java.isEmpty()) {
            mime2java.put("iso-2022-cn", "ISO2022CN");
            mime2java.put("iso-2022-kr", "ISO2022KR");
            mime2java.put("utf-8", "UTF8");
            mime2java.put("utf8", "UTF8");
            mime2java.put("ja_jp.iso2022-7", "ISO2022JP");
            mime2java.put("ja_jp.eucjp", "EUCJIS");
            mime2java.put("euc-kr", "KSC5601");
            mime2java.put("euckr", "KSC5601");
            mime2java.put("us-ascii", "ISO-8859-1");
            mime2java.put("x-us-ascii", "ISO-8859-1");
        }
    }
}

