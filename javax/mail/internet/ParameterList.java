/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.PropUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

public class ParameterList {
    private Map<String, Object> list = new LinkedHashMap<String, Object>();
    private Set<String> multisegmentNames;
    private Map<String, Object> slist;
    private String lastName = null;
    private static final boolean encodeParameters = PropUtil.getBooleanSystemProperty("mail.mime.encodeparameters", true);
    private static final boolean decodeParameters = PropUtil.getBooleanSystemProperty("mail.mime.decodeparameters", true);
    private static final boolean decodeParametersStrict = PropUtil.getBooleanSystemProperty("mail.mime.decodeparameters.strict", false);
    private static final boolean applehack = PropUtil.getBooleanSystemProperty("mail.mime.applefilenames", false);
    private static final boolean windowshack = PropUtil.getBooleanSystemProperty("mail.mime.windowsfilenames", false);
    private static final boolean parametersStrict = PropUtil.getBooleanSystemProperty("mail.mime.parameters.strict", true);
    private static final boolean splitLongParameters = PropUtil.getBooleanSystemProperty("mail.mime.splitlongparameters", true);
    private static final char[] hex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public ParameterList() {
        if (decodeParameters) {
            this.multisegmentNames = new HashSet<String>();
            this.slist = new HashMap<String, Object>();
        }
    }

    public ParameterList(String s) throws ParseException {
        this();
        HeaderTokenizer.Token tk;
        int type;
        HeaderTokenizer h = new HeaderTokenizer(s, "()<>@,;:\\\"\t []/?=");
        while ((type = (tk = h.next()).getType()) != -4) {
            String value;
            if ((char)type == ';') {
                tk = h.next();
                if (tk.getType() == -4) break;
                if (tk.getType() != -1) {
                    throw new ParseException("In parameter list <" + s + ">, expected parameter name, got \"" + tk.getValue() + "\"");
                }
                String name = tk.getValue().toLowerCase(Locale.ENGLISH);
                tk = h.next();
                if ((char)tk.getType() != '=') {
                    throw new ParseException("In parameter list <" + s + ">, expected '=', got \"" + tk.getValue() + "\"");
                }
                tk = windowshack && (name.equals("name") || name.equals("filename")) ? h.next(';', true) : (parametersStrict ? h.next() : h.next(';'));
                type = tk.getType();
                if (type != -1 && type != -2) {
                    throw new ParseException("In parameter list <" + s + ">, expected parameter value, got \"" + tk.getValue() + "\"");
                }
                value = tk.getValue();
                this.lastName = name;
                if (decodeParameters) {
                    this.putEncodedName(name, value);
                    continue;
                }
                this.list.put(name, value);
                continue;
            }
            if (type == -1 && this.lastName != null && (applehack && (this.lastName.equals("name") || this.lastName.equals("filename")) || !parametersStrict)) {
                String lastValue = (String)this.list.get(this.lastName);
                value = lastValue + " " + tk.getValue();
                this.list.put(this.lastName, value);
                continue;
            }
            throw new ParseException("In parameter list <" + s + ">, expected ';', got \"" + tk.getValue() + "\"");
        }
        if (decodeParameters) {
            this.combineMultisegmentNames(false);
        }
    }

    public void combineSegments() {
        if (decodeParameters && this.multisegmentNames.size() > 0) {
            try {
                this.combineMultisegmentNames(true);
            }
            catch (ParseException parseException) {
                // empty catch block
            }
        }
    }

    private void putEncodedName(String name, String value) throws ParseException {
        int star = name.indexOf(42);
        if (star < 0) {
            this.list.put(name, value);
        } else if (star == name.length() - 1) {
            Value v;
            block10: {
                name = name.substring(0, star);
                v = ParameterList.extractCharset(value);
                try {
                    v.value = ParameterList.decodeBytes(v.value, v.charset);
                }
                catch (UnsupportedEncodingException ex) {
                    if (!decodeParametersStrict) break block10;
                    throw new ParseException(ex.toString());
                }
            }
            this.list.put(name, v);
        } else {
            Object v;
            String rname = name.substring(0, star);
            this.multisegmentNames.add(rname);
            this.list.put(rname, "");
            if (name.endsWith("*")) {
                if (name.endsWith("*0*")) {
                    v = ParameterList.extractCharset(value);
                } else {
                    v = new Value();
                    ((Value)v).encodedValue = value;
                    ((Value)v).value = value;
                }
                name = name.substring(0, name.length() - 1);
            } else {
                v = value;
            }
            this.slist.put(name, v);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void combineMultisegmentNames(boolean keepConsistentOnFailure) throws ParseException {
        block26: {
            block27: {
                boolean success = false;
                try {
                    for (String name : this.multisegmentNames) {
                        String sname;
                        Object v;
                        MultiValue mv = new MultiValue();
                        String charset = null;
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int segment = 0;
                        while ((v = this.slist.get(sname = name + "*" + segment)) != null) {
                            mv.add(v);
                            try {
                                if (v instanceof Value) {
                                    Value vv = (Value)v;
                                    if (segment == 0) {
                                        charset = vv.charset;
                                    } else if (charset == null) {
                                        this.multisegmentNames.remove(name);
                                        break;
                                    }
                                    ParameterList.decodeBytes(vv.value, bos);
                                } else {
                                    bos.write(ASCIIUtility.getBytes((String)v));
                                }
                            }
                            catch (IOException iOException) {
                                // empty catch block
                            }
                            this.slist.remove(sname);
                            ++segment;
                        }
                        if (segment == 0) {
                            this.list.remove(name);
                            continue;
                        }
                        try {
                            if (charset != null) {
                                charset = MimeUtility.javaCharset(charset);
                            }
                            if (charset == null || charset.length() == 0) {
                                charset = MimeUtility.getDefaultJavaCharset();
                            }
                            mv.value = charset != null ? bos.toString(charset) : bos.toString();
                        }
                        catch (UnsupportedEncodingException uex) {
                            if (decodeParametersStrict) {
                                throw new ParseException(uex.toString());
                            }
                            try {
                                mv.value = bos.toString("iso-8859-1");
                            }
                            catch (UnsupportedEncodingException unsupportedEncodingException) {
                                // empty catch block
                            }
                        }
                        this.list.put(name, mv);
                    }
                    success = true;
                    Object var13_15 = null;
                    if (!keepConsistentOnFailure && !success) break block26;
                    if (this.slist.size() <= 0) break block27;
                }
                catch (Throwable throwable) {
                    Object var13_16 = null;
                    if (keepConsistentOnFailure || success) {
                        if (this.slist.size() > 0) {
                            for (Object v : this.slist.values()) {
                                if (!(v instanceof Value)) continue;
                                Value vv = (Value)v;
                                try {
                                    vv.value = ParameterList.decodeBytes(vv.value, vv.charset);
                                }
                                catch (UnsupportedEncodingException ex) {
                                    if (!decodeParametersStrict) continue;
                                    throw new ParseException(ex.toString());
                                }
                            }
                            this.list.putAll(this.slist);
                        }
                        this.multisegmentNames.clear();
                        this.slist.clear();
                    }
                    throw throwable;
                }
                for (Object v : this.slist.values()) {
                    if (!(v instanceof Value)) continue;
                    Value vv = (Value)v;
                    try {
                        vv.value = ParameterList.decodeBytes(vv.value, vv.charset);
                    }
                    catch (UnsupportedEncodingException ex) {
                        if (!decodeParametersStrict) continue;
                        throw new ParseException(ex.toString());
                    }
                }
                this.list.putAll(this.slist);
            }
            this.multisegmentNames.clear();
            this.slist.clear();
            {
            }
        }
    }

    public int size() {
        return this.list.size();
    }

    public String get(String name) {
        Object v = this.list.get(name.trim().toLowerCase(Locale.ENGLISH));
        String value = v instanceof MultiValue ? ((MultiValue)v).value : (v instanceof LiteralValue ? ((LiteralValue)v).value : (v instanceof Value ? ((Value)v).value : (String)v));
        return value;
    }

    public void set(String name, String value) {
        name = name.trim().toLowerCase(Locale.ENGLISH);
        if (decodeParameters) {
            try {
                this.putEncodedName(name, value);
            }
            catch (ParseException pex) {
                this.list.put(name, value);
            }
        } else {
            this.list.put(name, value);
        }
    }

    public void set(String name, String value, String charset) {
        if (encodeParameters) {
            Value ev = ParameterList.encodeValue(value, charset);
            if (ev != null) {
                this.list.put(name.trim().toLowerCase(Locale.ENGLISH), ev);
            } else {
                this.set(name, value);
            }
        } else {
            this.set(name, value);
        }
    }

    void setLiteral(String name, String value) {
        LiteralValue lv = new LiteralValue();
        lv.value = value;
        this.list.put(name, lv);
    }

    public void remove(String name) {
        this.list.remove(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public Enumeration getNames() {
        return new ParamEnum(this.list.keySet().iterator());
    }

    public String toString() {
        return this.toString(0);
    }

    public String toString(int used) {
        ToStringBuffer sb = new ToStringBuffer(used);
        for (Map.Entry<String, Object> ent : this.list.entrySet()) {
            String value;
            String name = ent.getKey();
            Object v = ent.getValue();
            if (v instanceof MultiValue) {
                MultiValue vv = (MultiValue)v;
                name = name + "*";
                for (int i = 0; i < vv.size(); ++i) {
                    String ns;
                    Object va = vv.get(i);
                    if (va instanceof Value) {
                        ns = name + i + "*";
                        value = ((Value)va).encodedValue;
                    } else {
                        ns = name + i;
                        value = (String)va;
                    }
                    sb.addNV(ns, ParameterList.quote(value));
                }
                continue;
            }
            if (v instanceof LiteralValue) {
                value = ((LiteralValue)v).value;
                sb.addNV(name, ParameterList.quote(value));
                continue;
            }
            if (v instanceof Value) {
                name = name + "*";
                value = ((Value)v).encodedValue;
                sb.addNV(name, ParameterList.quote(value));
                continue;
            }
            value = (String)v;
            if (value.length() > 60 && splitLongParameters && encodeParameters) {
                int seg = 0;
                name = name + "*";
                while (value.length() > 60) {
                    sb.addNV(name + seg, ParameterList.quote(value.substring(0, 60)));
                    value = value.substring(60);
                    ++seg;
                }
                if (value.length() <= 0) continue;
                sb.addNV(name + seg, ParameterList.quote(value));
                continue;
            }
            sb.addNV(name, ParameterList.quote(value));
        }
        return sb.toString();
    }

    private static String quote(String value) {
        return MimeUtility.quote(value, "()<>@,;:\\\"\t []/?=");
    }

    private static Value encodeValue(String value, String charset) {
        byte[] b;
        if (MimeUtility.checkAscii(value) == 1) {
            return null;
        }
        try {
            b = value.getBytes(MimeUtility.javaCharset(charset));
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
        StringBuffer sb = new StringBuffer(b.length + charset.length() + 2);
        sb.append(charset).append("''");
        for (int i = 0; i < b.length; ++i) {
            char c = (char)(b[i] & 0xFF);
            if (c <= ' ' || c >= '\u007f' || c == '*' || c == '\'' || c == '%' || "()<>@,;:\\\"\t []/?=".indexOf(c) >= 0) {
                sb.append('%').append(hex[c >> 4]).append(hex[c & 0xF]);
                continue;
            }
            sb.append(c);
        }
        Value v = new Value();
        v.charset = charset;
        v.value = value;
        v.encodedValue = sb.toString();
        return v;
    }

    private static Value extractCharset(String value) throws ParseException {
        Value v;
        block8: {
            v = new Value();
            v.value = v.encodedValue = value;
            try {
                int i = value.indexOf(39);
                if (i < 0) {
                    if (decodeParametersStrict) {
                        throw new ParseException("Missing charset in encoded value: " + value);
                    }
                    return v;
                }
                String charset = value.substring(0, i);
                int li = value.indexOf(39, i + 1);
                if (li < 0) {
                    if (decodeParametersStrict) {
                        throw new ParseException("Missing language in encoded value: " + value);
                    }
                    return v;
                }
                v.value = value.substring(li + 1);
                v.charset = charset;
            }
            catch (NumberFormatException nex) {
                if (decodeParametersStrict) {
                    throw new ParseException(nex.toString());
                }
            }
            catch (StringIndexOutOfBoundsException ex) {
                if (!decodeParametersStrict) break block8;
                throw new ParseException(ex.toString());
            }
        }
        return v;
    }

    private static String decodeBytes(String value, String charset) throws ParseException, UnsupportedEncodingException {
        byte[] b = new byte[value.length()];
        int bi = 0;
        for (int i = 0; i < value.length(); ++i) {
            char c;
            block8: {
                c = value.charAt(i);
                if (c == '%') {
                    try {
                        String hex = value.substring(i + 1, i + 3);
                        c = (char)Integer.parseInt(hex, 16);
                        i += 2;
                    }
                    catch (NumberFormatException ex) {
                        if (decodeParametersStrict) {
                            throw new ParseException(ex.toString());
                        }
                    }
                    catch (StringIndexOutOfBoundsException ex) {
                        if (!decodeParametersStrict) break block8;
                        throw new ParseException(ex.toString());
                    }
                }
            }
            b[bi++] = (byte)c;
        }
        if (charset != null) {
            charset = MimeUtility.javaCharset(charset);
        }
        if (charset == null || charset.length() == 0) {
            charset = MimeUtility.getDefaultJavaCharset();
        }
        return new String(b, 0, bi, charset);
    }

    private static void decodeBytes(String value, OutputStream os) throws ParseException, IOException {
        for (int i = 0; i < value.length(); ++i) {
            char c;
            block6: {
                c = value.charAt(i);
                if (c == '%') {
                    try {
                        String hex = value.substring(i + 1, i + 3);
                        c = (char)Integer.parseInt(hex, 16);
                        i += 2;
                    }
                    catch (NumberFormatException ex) {
                        if (decodeParametersStrict) {
                            throw new ParseException(ex.toString());
                        }
                    }
                    catch (StringIndexOutOfBoundsException ex) {
                        if (!decodeParametersStrict) break block6;
                        throw new ParseException(ex.toString());
                    }
                }
            }
            os.write((byte)c);
        }
    }

    private static class ToStringBuffer {
        private int used;
        private StringBuffer sb = new StringBuffer();

        public ToStringBuffer(int used) {
            this.used = used;
        }

        public void addNV(String name, String value) {
            this.sb.append("; ");
            this.used += 2;
            int len = name.length() + value.length() + 1;
            if (this.used + len > 76) {
                this.sb.append("\r\n\t");
                this.used = 8;
            }
            this.sb.append(name).append('=');
            this.used += name.length() + 1;
            if (this.used + value.length() > 76) {
                String s = MimeUtility.fold(this.used, value);
                this.sb.append(s);
                int lastlf = s.lastIndexOf(10);
                this.used = lastlf >= 0 ? (this.used += s.length() - lastlf - 1) : (this.used += s.length());
            } else {
                this.sb.append(value);
                this.used += value.length();
            }
        }

        public String toString() {
            return this.sb.toString();
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class ParamEnum
    implements Enumeration<String> {
        private Iterator<String> it;

        ParamEnum(Iterator<String> it) {
            this.it = it;
        }

        @Override
        public boolean hasMoreElements() {
            return this.it.hasNext();
        }

        @Override
        public String nextElement() {
            return this.it.next();
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class MultiValue
    extends ArrayList<Object> {
        private static final long serialVersionUID = 699561094618751023L;
        String value;

        private MultiValue() {
        }
    }

    private static class LiteralValue {
        String value;

        private LiteralValue() {
        }
    }

    private static class Value {
        String value;
        String charset;
        String encodedValue;

        private Value() {
        }
    }
}

