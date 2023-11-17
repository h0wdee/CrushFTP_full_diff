/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

public class URLUTF8Encoder {
    static final String[] hex = new String[]{"%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F", "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F", "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29", "%2A", "%2B", "%2C", "%2D", "%2E", "%2F", "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37", "%38", "%39", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F", "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47", "%48", "%49", "%4A", "%4B", "%4C", "%4D", "%4E", "%4F", "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57", "%58", "%59", "%5A", "%5B", "%5C", "%5D", "%5E", "%5F", "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67", "%68", "%69", "%6A", "%6B", "%6C", "%6D", "%6E", "%6F", "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77", "%78", "%79", "%7A", "%7B", "%7C", "%7D", "%7E", "%7F", "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87", "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F", "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97", "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F", "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7", "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF", "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7", "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF", "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7", "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF", "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7", "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF", "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7", "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF", "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7", "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"};

    public static String encode(String s, boolean encodePathSeperator) {
        StringBuffer sbuf = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (ch == '/' && !encodePathSeperator) {
                sbuf.append(ch);
                continue;
            }
            if ('A' <= ch && ch <= 'Z') {
                sbuf.append(ch);
                continue;
            }
            if ('a' <= ch && ch <= 'z') {
                sbuf.append(ch);
                continue;
            }
            if ('0' <= ch && ch <= '9') {
                sbuf.append(ch);
                continue;
            }
            if (ch == ' ') {
                sbuf.append("%20");
                continue;
            }
            if (ch == '-' || ch == '_' || ch == '.' || ch == '!' || ch == '~' || ch == '*' || ch == '\'' || ch == '(' || ch == ')') {
                sbuf.append(ch);
                continue;
            }
            if (ch <= '\u007f') {
                sbuf.append(hex[ch]);
                continue;
            }
            if (ch <= '\u07ff') {
                sbuf.append(hex[0xC0 | ch >> 6]);
                sbuf.append(hex[0x80 | ch & 0x3F]);
                continue;
            }
            sbuf.append(hex[0xE0 | ch >> 12]);
            sbuf.append(hex[0x80 | ch >> 6 & 0x3F]);
            sbuf.append(hex[0x80 | ch & 0x3F]);
        }
        return sbuf.toString();
    }

    public static String decode(String s) {
        StringBuffer sbuf = new StringBuffer();
        int l = s.length();
        int ch = -1;
        int sumb = 0;
        int more = -1;
        for (int i = 0; i < l; ++i) {
            int b;
            char c = s.charAt(i);
            ch = c;
            switch (c) {
                case '%': {
                    ch = s.charAt(++i);
                    int hb = (Character.isDigit((char)ch) ? ch - 48 : 10 + Character.toLowerCase((char)ch) - 97) & 0xF;
                    ch = s.charAt(++i);
                    int lb = (Character.isDigit((char)ch) ? ch - 48 : 10 + Character.toLowerCase((char)ch) - 97) & 0xF;
                    b = hb << 4 | lb;
                    break;
                }
                case '+': {
                    b = 32;
                    break;
                }
                default: {
                    b = ch;
                }
            }
            if ((b & 0xC0) == 128) {
                sumb = sumb << 6 | b & 0x3F;
                if (--more != 0) continue;
                sbuf.append((char)sumb);
                continue;
            }
            if ((b & 0x80) == 0) {
                sbuf.append((char)b);
                continue;
            }
            if ((b & 0xE0) == 192) {
                sumb = b & 0x1F;
                more = 1;
                continue;
            }
            if ((b & 0xF0) == 224) {
                sumb = b & 0xF;
                more = 2;
                continue;
            }
            if ((b & 0xF8) == 240) {
                sumb = b & 7;
                more = 3;
                continue;
            }
            if ((b & 0xFC) == 248) {
                sumb = b & 3;
                more = 4;
                continue;
            }
            sumb = b & 1;
            more = 5;
        }
        return sbuf.toString();
    }
}

