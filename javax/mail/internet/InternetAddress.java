/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.internet;

import com.sun.mail.util.PropUtil;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeUtility;

public class InternetAddress
extends Address
implements Cloneable {
    protected String address;
    protected String personal;
    protected String encodedPersonal;
    private static final long serialVersionUID = -7507595530758302903L;
    private static final boolean ignoreBogusGroupName = PropUtil.getBooleanSystemProperty("mail.mime.address.ignorebogusgroupname", true);
    private static final boolean useCanonicalHostName = PropUtil.getBooleanSystemProperty("mail.mime.address.usecanonicalhostname", true);
    private static final String rfc822phrase = "()<>@,;:\\\"\t .[]".replace(' ', '\u0000').replace('\t', '\u0000');
    private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";
    private static final String specialsNoDot = "()<>,;:\\\"[]@";

    public InternetAddress() {
    }

    public InternetAddress(String address) throws AddressException {
        InternetAddress[] a = InternetAddress.parse(address, true);
        if (a.length != 1) {
            throw new AddressException("Illegal address", address);
        }
        this.address = a[0].address;
        this.personal = a[0].personal;
        this.encodedPersonal = a[0].encodedPersonal;
    }

    public InternetAddress(String address, boolean strict) throws AddressException {
        this(address);
        if (strict) {
            if (this.isGroup()) {
                this.getGroup(true);
            } else {
                InternetAddress.checkAddress(this.address, true, true);
            }
        }
    }

    public InternetAddress(String address, String personal) throws UnsupportedEncodingException {
        this(address, personal, null);
    }

    public InternetAddress(String address, String personal, String charset) throws UnsupportedEncodingException {
        this.address = address;
        this.setPersonal(personal, charset);
    }

    public Object clone() {
        InternetAddress a = null;
        try {
            a = (InternetAddress)super.clone();
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            // empty catch block
        }
        return a;
    }

    public String getType() {
        return "rfc822";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPersonal(String name, String charset) throws UnsupportedEncodingException {
        this.personal = name;
        this.encodedPersonal = name != null ? MimeUtility.encodeWord(name, charset, null) : null;
    }

    public void setPersonal(String name) throws UnsupportedEncodingException {
        this.personal = name;
        this.encodedPersonal = name != null ? MimeUtility.encodeWord(name) : null;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPersonal() {
        if (this.personal != null) {
            return this.personal;
        }
        if (this.encodedPersonal != null) {
            try {
                this.personal = MimeUtility.decodeText(this.encodedPersonal);
                return this.personal;
            }
            catch (Exception ex) {
                return this.encodedPersonal;
            }
        }
        return null;
    }

    public String toString() {
        String a;
        String string = a = this.address == null ? "" : this.address;
        if (this.encodedPersonal == null && this.personal != null) {
            try {
                this.encodedPersonal = MimeUtility.encodeWord(this.personal);
            }
            catch (UnsupportedEncodingException unsupportedEncodingException) {
                // empty catch block
            }
        }
        if (this.encodedPersonal != null) {
            return InternetAddress.quotePhrase(this.encodedPersonal) + " <" + a + ">";
        }
        if (this.isGroup() || this.isSimple()) {
            return a;
        }
        return "<" + a + ">";
    }

    public String toUnicodeString() {
        String p = this.getPersonal();
        if (p != null) {
            return InternetAddress.quotePhrase(p) + " <" + this.address + ">";
        }
        if (this.isGroup() || this.isSimple()) {
            return this.address;
        }
        return "<" + this.address + ">";
    }

    private static String quotePhrase(String phrase) {
        int len = phrase.length();
        boolean needQuoting = false;
        for (int i = 0; i < len; ++i) {
            char c = phrase.charAt(i);
            if (c == '\"' || c == '\\') {
                StringBuffer sb = new StringBuffer(len + 3);
                sb.append('\"');
                for (int j = 0; j < len; ++j) {
                    char cc = phrase.charAt(j);
                    if (cc == '\"' || cc == '\\') {
                        sb.append('\\');
                    }
                    sb.append(cc);
                }
                sb.append('\"');
                return sb.toString();
            }
            if ((c >= ' ' || c == '\r' || c == '\n' || c == '\t') && c < '\u007f' && rfc822phrase.indexOf(c) < 0) continue;
            needQuoting = true;
        }
        if (needQuoting) {
            StringBuffer sb = new StringBuffer(len + 2);
            sb.append('\"').append(phrase).append('\"');
            return sb.toString();
        }
        return phrase;
    }

    private static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1 && (s = s.substring(1, s.length() - 1)).indexOf(92) >= 0) {
            StringBuffer sb = new StringBuffer(s.length());
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (c == '\\' && i < s.length() - 1) {
                    c = s.charAt(++i);
                }
                sb.append(c);
            }
            s = sb.toString();
        }
        return s;
    }

    public boolean equals(Object a) {
        if (!(a instanceof InternetAddress)) {
            return false;
        }
        String s = ((InternetAddress)a).getAddress();
        if (s == this.address) {
            return true;
        }
        return this.address != null && this.address.equalsIgnoreCase(s);
    }

    public int hashCode() {
        if (this.address == null) {
            return 0;
        }
        return this.address.toLowerCase(Locale.ENGLISH).hashCode();
    }

    public static String toString(Address[] addresses) {
        return InternetAddress.toString(addresses, 0);
    }

    public static String toString(Address[] addresses, int used) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addresses.length; ++i) {
            String s;
            int len;
            if (i != 0) {
                sb.append(", ");
                used += 2;
            }
            if (used + (len = InternetAddress.lengthOfFirstSegment(s = MimeUtility.fold(0, addresses[i].toString()))) > 76) {
                int curlen = sb.length();
                if (curlen > 0 && sb.charAt(curlen - 1) == ' ') {
                    sb.setLength(curlen - 1);
                }
                sb.append("\r\n\t");
                used = 8;
            }
            sb.append(s);
            used = InternetAddress.lengthOfLastSegment(s, used);
        }
        return sb.toString();
    }

    private static int lengthOfFirstSegment(String s) {
        int pos = s.indexOf("\r\n");
        if (pos != -1) {
            return pos;
        }
        return s.length();
    }

    private static int lengthOfLastSegment(String s, int used) {
        int pos = s.lastIndexOf("\r\n");
        if (pos != -1) {
            return s.length() - pos - 2;
        }
        return s.length() + used;
    }

    public static InternetAddress getLocalAddress(Session session) {
        try {
            return InternetAddress._getLocalAddress(session);
        }
        catch (SecurityException securityException) {
        }
        catch (AddressException addressException) {
        }
        catch (UnknownHostException unknownHostException) {
            // empty catch block
        }
        return null;
    }

    static InternetAddress _getLocalAddress(Session session) throws SecurityException, AddressException, UnknownHostException {
        String user = null;
        String host = null;
        String address = null;
        if (session == null) {
            user = System.getProperty("user.name");
            host = InternetAddress.getLocalHostName();
        } else {
            address = session.getProperty("mail.from");
            if (address == null) {
                user = session.getProperty("mail.user");
                if (user == null || user.length() == 0) {
                    user = session.getProperty("user.name");
                }
                if (user == null || user.length() == 0) {
                    user = System.getProperty("user.name");
                }
                if ((host = session.getProperty("mail.host")) == null || host.length() == 0) {
                    host = InternetAddress.getLocalHostName();
                }
            }
        }
        if (address == null && user != null && user.length() != 0 && host != null && host.length() != 0) {
            address = MimeUtility.quote(user.trim(), "()<>,;:\\\"[]@\t ") + "@" + host;
        }
        if (address == null) {
            return null;
        }
        return new InternetAddress(address);
    }

    private static String getLocalHostName() throws UnknownHostException {
        String host = null;
        InetAddress me = InetAddress.getLocalHost();
        if (me != null) {
            if (useCanonicalHostName) {
                host = me.getCanonicalHostName();
            }
            if (host == null) {
                host = me.getHostName();
            }
            if (host == null) {
                host = me.getHostAddress();
            }
            if (host != null && host.length() > 0 && InternetAddress.isInetAddressLiteral(host)) {
                host = '[' + host + ']';
            }
        }
        return host;
    }

    private static boolean isInetAddressLiteral(String addr) {
        boolean sawHex = false;
        boolean sawColon = false;
        for (int i = 0; i < addr.length(); ++i) {
            char c = addr.charAt(i);
            if (c >= '0' && c <= '9' || c == '.') continue;
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                sawHex = true;
                continue;
            }
            if (c == ':') {
                sawColon = true;
                continue;
            }
            return false;
        }
        return !sawHex || sawColon;
    }

    public static InternetAddress[] parse(String addresslist) throws AddressException {
        return InternetAddress.parse(addresslist, true);
    }

    public static InternetAddress[] parse(String addresslist, boolean strict) throws AddressException {
        return InternetAddress.parse(addresslist, strict, false);
    }

    public static InternetAddress[] parseHeader(String addresslist, boolean strict) throws AddressException {
        return InternetAddress.parse(MimeUtility.unfold(addresslist), strict, true);
    }

    /*
     * Unable to fully structure code
     */
    private static InternetAddress[] parse(String s, boolean strict, boolean parseHdr) throws AddressException {
        start_personal = -1;
        end_personal = -1;
        length = s.length();
        ignoreErrors = parseHdr != false && strict == false;
        in_group = false;
        route_addr = false;
        rfc822 = false;
        v = new ArrayList<InternetAddress>();
        end = -1;
        start = -1;
        block30: for (index = 0; index < length; ++index) {
            c = s.charAt(index);
            switch (c) {
                case '(': {
                    rfc822 = true;
                    if (start >= 0 && end == -1) {
                        end = index;
                    }
                    pindex = index++;
                    nesting = 1;
                    while (index < length && nesting > 0) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                break;
                            }
                            case '(': {
                                ++nesting;
                                break;
                            }
                            case ')': {
                                --nesting;
                                break;
                            }
                        }
                        ++index;
                    }
                    if (nesting > 0) {
                        if (!ignoreErrors) {
                            throw new AddressException("Missing ')'", s, index);
                        }
                        index = pindex + 1;
                        continue block30;
                    }
                    --index;
                    if (start_personal == -1) {
                        start_personal = pindex + 1;
                    }
                    if (end_personal != -1) continue block30;
                    end_personal = index;
                    continue block30;
                }
                case ')': {
                    if (!ignoreErrors) {
                        throw new AddressException("Missing '('", s, index);
                    }
                    if (start != -1) continue block30;
                    start = index;
                    continue block30;
                }
                case '<': {
                    rfc822 = true;
                    if (route_addr) {
                        if (!ignoreErrors) {
                            throw new AddressException("Extra route-addr", s, index);
                        }
                        if (start == -1) {
                            route_addr = false;
                            rfc822 = false;
                            end = -1;
                            start = -1;
                            continue block30;
                        }
                        if (!in_group) {
                            if (end == -1) {
                                end = index;
                            }
                            addr = s.substring(start, end).trim();
                            ma = new InternetAddress();
                            ma.setAddress(addr);
                            if (start_personal >= 0) {
                                ma.encodedPersonal = InternetAddress.unquote(s.substring(start_personal, end_personal).trim());
                            }
                            v.add(ma);
                            route_addr = false;
                            rfc822 = false;
                            end = -1;
                            start = -1;
                            end_personal = -1;
                            start_personal = -1;
                        }
                    }
                    rindex = index++;
                    inquote = false;
                    block32: while (index < length) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                ** GOTO lbl91
                            }
                            case '\"': {
                                inquote = inquote == false;
                                ** GOTO lbl91
                            }
                            case '>': {
                                if (!inquote) break block32;
                            }
lbl91:
                            // 4 sources

                            default: {
                                ++index;
                                continue block32;
                            }
                        }
                    }
                    if (inquote) {
                        if (!ignoreErrors) {
                            throw new AddressException("Missing '\"'", s, index);
                        }
                        for (index = rindex + 1; index < length; ++index) {
                            c = s.charAt(index);
                            if (c == '\\') {
                                ++index;
                                continue;
                            }
                            if (c == '>') break;
                        }
                    }
                    if (index >= length) {
                        if (!ignoreErrors) {
                            throw new AddressException("Missing '>'", s, index);
                        }
                        index = rindex + 1;
                        if (start != -1) continue block30;
                        start = rindex;
                        continue block30;
                    }
                    if (!in_group) {
                        if (start >= 0) {
                            start_personal = start;
                            end_personal = rindex;
                        }
                        start = rindex + 1;
                    }
                    route_addr = true;
                    end = index;
                    continue block30;
                }
                case '>': {
                    if (!ignoreErrors) {
                        throw new AddressException("Missing '<'", s, index);
                    }
                    if (start != -1) continue block30;
                    start = index;
                    continue block30;
                }
                case '\"': {
                    qindex = index;
                    rfc822 = true;
                    if (start == -1) {
                        start = index;
                    }
                    ++index;
                    block34: while (index < length) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                ** GOTO lbl139
                            }
                            case '\"': {
                                break block34;
                            }
lbl139:
                            // 2 sources

                            default: {
                                ++index;
                                continue block34;
                            }
                        }
                    }
                    if (index < length) continue block30;
                    if (!ignoreErrors) {
                        throw new AddressException("Missing '\"'", s, index);
                    }
                    index = qindex + 1;
                    continue block30;
                }
                case '[': {
                    rfc822 = true;
                    lindex = index++;
                    block35: while (index < length) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                ** GOTO lbl158
                            }
                            case ']': {
                                break block35;
                            }
lbl158:
                            // 2 sources

                            default: {
                                ++index;
                                continue block35;
                            }
                        }
                    }
                    if (index < length) continue block30;
                    if (!ignoreErrors) {
                        throw new AddressException("Missing ']'", s, index);
                    }
                    index = lindex + 1;
                    continue block30;
                }
                case ';': {
                    if (start == -1) {
                        route_addr = false;
                        rfc822 = false;
                        end = -1;
                        start = -1;
                        continue block30;
                    }
                    if (in_group) {
                        in_group = false;
                        if (parseHdr && !strict && index + 1 < length && s.charAt(index + 1) == '@') continue block30;
                        ma = new InternetAddress();
                        end = index + 1;
                        ma.setAddress(s.substring(start, end).trim());
                        v.add(ma);
                        route_addr = false;
                        rfc822 = false;
                        end = -1;
                        start = -1;
                        end_personal = -1;
                        start_personal = -1;
                        continue block30;
                    }
                    if (!ignoreErrors) {
                        throw new AddressException("Illegal semicolon, not in group", s, index);
                    }
                }
                case ',': {
                    if (start == -1) {
                        route_addr = false;
                        rfc822 = false;
                        end = -1;
                        start = -1;
                        continue block30;
                    }
                    if (in_group) {
                        route_addr = false;
                        continue block30;
                    }
                    if (end == -1) {
                        end = index;
                    }
                    addr = s.substring(start, end).trim();
                    pers = null;
                    if (rfc822 && start_personal >= 0 && (pers = InternetAddress.unquote(s.substring(start_personal, end_personal).trim())).trim().length() == 0) {
                        pers = null;
                    }
                    if (parseHdr && !strict && pers != null && pers.indexOf(64) >= 0 && addr.indexOf(64) < 0 && addr.indexOf(33) < 0) {
                        tmp = addr;
                        addr = pers;
                        pers = tmp;
                    }
                    if (rfc822 || strict || parseHdr) {
                        if (!ignoreErrors) {
                            InternetAddress.checkAddress(addr, route_addr, false);
                        }
                        ma = new InternetAddress();
                        ma.setAddress(addr);
                        if (pers != null) {
                            ma.encodedPersonal = pers;
                        }
                        v.add(ma);
                    } else {
                        st = new StringTokenizer(addr);
                        while (st.hasMoreTokens()) {
                            a = st.nextToken();
                            InternetAddress.checkAddress(a, false, false);
                            ma = new InternetAddress();
                            ma.setAddress(a);
                            v.add(ma);
                        }
                    }
                    route_addr = false;
                    rfc822 = false;
                    end = -1;
                    start = -1;
                    end_personal = -1;
                    start_personal = -1;
                    continue block30;
                }
                case ':': {
                    rfc822 = true;
                    if (in_group && !ignoreErrors) {
                        throw new AddressException("Nested group", s, index);
                    }
                    if (start == -1) {
                        start = index;
                    }
                    if (parseHdr && !strict) {
                        if (index + 1 < length && (addressSpecials = ")>[]:@\\,.").indexOf(nc = s.charAt(index + 1)) >= 0) {
                            if (nc != '@') continue block30;
                            for (i = index + 2; i < length && (nc = s.charAt(i)) != ';' && addressSpecials.indexOf(nc) < 0; ++i) {
                            }
                            if (nc == ';') continue block30;
                        }
                        gname = s.substring(start, index);
                        if (InternetAddress.ignoreBogusGroupName && (gname.equalsIgnoreCase("mailto") || gname.equalsIgnoreCase("From") || gname.equalsIgnoreCase("To") || gname.equalsIgnoreCase("Cc") || gname.equalsIgnoreCase("Subject") || gname.equalsIgnoreCase("Re"))) {
                            start = -1;
                            continue block30;
                        }
                        in_group = true;
                        continue block30;
                    }
                    in_group = true;
                    continue block30;
                }
                case '\t': 
                case '\n': 
                case '\r': 
                case ' ': {
                    continue block30;
                }
                default: {
                    if (start != -1) continue block30;
                    start = index;
                }
            }
        }
        if (start >= 0) {
            if (end == -1) {
                end = length;
            }
            addr = s.substring(start, end).trim();
            pers = null;
            if (rfc822 && start_personal >= 0 && (pers = InternetAddress.unquote(s.substring(start_personal, end_personal).trim())).trim().length() == 0) {
                pers = null;
            }
            if (parseHdr && !strict && pers != null && pers.indexOf(64) >= 0 && addr.indexOf(64) < 0 && addr.indexOf(33) < 0) {
                tmp = addr;
                addr = pers;
                pers = tmp;
            }
            if (rfc822 || strict || parseHdr) {
                if (!ignoreErrors) {
                    InternetAddress.checkAddress(addr, route_addr, false);
                }
                ma = new InternetAddress();
                ma.setAddress(addr);
                if (pers != null) {
                    ma.encodedPersonal = pers;
                }
                v.add(ma);
            } else {
                st = new StringTokenizer(addr);
                while (st.hasMoreTokens()) {
                    a = st.nextToken();
                    InternetAddress.checkAddress(a, false, false);
                    ma = new InternetAddress();
                    ma.setAddress(a);
                    v.add(ma);
                }
            }
        }
        a = new InternetAddress[v.size()];
        v.toArray(a);
        return a;
    }

    public void validate() throws AddressException {
        if (this.isGroup()) {
            this.getGroup(true);
        } else {
            InternetAddress.checkAddress(this.getAddress(), true, true);
        }
    }

    private static void checkAddress(String addr, boolean routeAddr, boolean validate) throws AddressException {
        int i;
        int start = 0;
        if (addr == null) {
            throw new AddressException("Address is null");
        }
        int len = addr.length();
        if (len == 0) {
            throw new AddressException("Empty address", addr);
        }
        if (routeAddr && addr.charAt(0) == '@') {
            start = 0;
            while ((i = InternetAddress.indexOfAny(addr, ",:", start)) >= 0) {
                if (addr.charAt(start) != '@') {
                    throw new AddressException("Illegal route-addr", addr);
                }
                if (addr.charAt(i) == ':') {
                    start = i + 1;
                    break;
                }
                start = i + 1;
            }
        }
        char c = '\uffff';
        char lastc = '\uffff';
        boolean inquote = false;
        for (i = start; i < len; ++i) {
            lastc = c;
            c = addr.charAt(i);
            if (c == 92 || lastc == '\\') continue;
            if (c == '\"') {
                if (inquote) {
                    if (validate && i + 1 < len && addr.charAt(i + 1) != '@') {
                        throw new AddressException("Quote not at end of local address", addr);
                    }
                    inquote = false;
                    continue;
                }
                if (validate && i != 0) {
                    throw new AddressException("Quote not at start of local address", addr);
                }
                inquote = true;
                continue;
            }
            if (c == '\r') {
                if (i + 1 < len && addr.charAt(i + 1) != '\n') {
                    throw new AddressException("Quoted local address contains CR without LF", addr);
                }
            } else if (c == '\n' && i + 1 < len && addr.charAt(i + 1) != ' ' && addr.charAt(i + 1) != '\t') {
                throw new AddressException("Quoted local address contains newline without whitespace", addr);
            }
            if (inquote) continue;
            if (c == '@') {
                if (i != 0) break;
                throw new AddressException("Missing local name", addr);
            }
            if (c <= ' ' || c >= '\u007f') {
                throw new AddressException("Local address contains control or whitespace", addr);
            }
            if (specialsNoDot.indexOf(c) < 0) continue;
            throw new AddressException("Local address contains illegal character", addr);
        }
        if (inquote) {
            throw new AddressException("Unterminated quote", addr);
        }
        if (c != '@') {
            if (validate) {
                throw new AddressException("Missing final '@domain'", addr);
            }
            return;
        }
        start = i + 1;
        if (start >= len) {
            throw new AddressException("Missing domain", addr);
        }
        if (addr.charAt(start) == '.') {
            throw new AddressException("Domain starts with dot", addr);
        }
        boolean inliteral = false;
        for (i = start; i < len; ++i) {
            c = addr.charAt(i);
            if (c == '[') {
                if (i != start) {
                    throw new AddressException("Domain literal not at start of domain", addr);
                }
                inliteral = true;
            } else if (c == ']') {
                if (i != len - 1) {
                    throw new AddressException("Domain literal end not at end of domain", addr);
                }
                inliteral = false;
            } else {
                if (c <= ' ' || c >= '\u007f') {
                    throw new AddressException("Domain contains control or whitespace", addr);
                }
                if (!inliteral) {
                    if (!Character.isLetterOrDigit(c) && c != '-' && c != '.') {
                        throw new AddressException("Domain contains illegal character", addr);
                    }
                    if (c == '.' && lastc == '.') {
                        throw new AddressException("Domain contains dot-dot", addr);
                    }
                }
            }
            lastc = c;
        }
        if (lastc == '.') {
            throw new AddressException("Domain ends with dot", addr);
        }
    }

    private boolean isSimple() {
        return this.address == null || InternetAddress.indexOfAny(this.address, specialsNoDotNoAt) < 0;
    }

    public boolean isGroup() {
        return this.address != null && this.address.endsWith(";") && this.address.indexOf(58) > 0;
    }

    public InternetAddress[] getGroup(boolean strict) throws AddressException {
        String addr = this.getAddress();
        if (addr == null) {
            return null;
        }
        if (!addr.endsWith(";")) {
            return null;
        }
        int ix = addr.indexOf(58);
        if (ix < 0) {
            return null;
        }
        String list = addr.substring(ix + 1, addr.length() - 1);
        return InternetAddress.parseHeader(list, strict);
    }

    private static int indexOfAny(String s, String any) {
        return InternetAddress.indexOfAny(s, any, 0);
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
}

