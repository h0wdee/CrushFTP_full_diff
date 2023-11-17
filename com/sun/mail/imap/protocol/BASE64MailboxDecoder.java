/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap.protocol;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class BASE64MailboxDecoder {
    static final char[] pem_array;
    private static final byte[] pem_convert_array;

    public static String decode(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        boolean changedString = false;
        int copyTo = 0;
        char[] chars = new char[original.length()];
        StringCharacterIterator iter = new StringCharacterIterator(original);
        char c = iter.first();
        while (c != '\uffff') {
            if (c == '&') {
                changedString = true;
                copyTo = BASE64MailboxDecoder.base64decode(chars, copyTo, iter);
            } else {
                chars[copyTo++] = c;
            }
            c = iter.next();
        }
        if (changedString) {
            return new String(chars, 0, copyTo);
        }
        return original;
    }

    protected static int base64decode(char[] buffer, int offset, CharacterIterator iter) {
        byte orig_0;
        boolean firsttime = true;
        int leftover = -1;
        while ((orig_0 = (byte)iter.next()) != -1) {
            byte orig_3;
            byte orig_2;
            if (orig_0 == 45) {
                if (!firsttime) break;
                buffer[offset++] = 38;
                break;
            }
            firsttime = false;
            byte orig_1 = (byte)iter.next();
            if (orig_1 == -1 || orig_1 == 45) break;
            byte a = pem_convert_array[orig_0 & 0xFF];
            byte b = pem_convert_array[orig_1 & 0xFF];
            byte current = (byte)(a << 2 & 0xFC | b >>> 4 & 3);
            if (leftover != -1) {
                buffer[offset++] = (char)(leftover << 8 | current & 0xFF);
                leftover = -1;
            } else {
                leftover = current & 0xFF;
            }
            if ((orig_2 = (byte)iter.next()) == 61) continue;
            if (orig_2 == -1 || orig_2 == 45) break;
            a = b;
            b = pem_convert_array[orig_2 & 0xFF];
            current = (byte)(a << 4 & 0xF0 | b >>> 2 & 0xF);
            if (leftover != -1) {
                buffer[offset++] = (char)(leftover << 8 | current & 0xFF);
                leftover = -1;
            } else {
                leftover = current & 0xFF;
            }
            if ((orig_3 = (byte)iter.next()) == 61) continue;
            if (orig_3 == -1 || orig_3 == 45) break;
            a = b;
            b = pem_convert_array[orig_3 & 0xFF];
            current = (byte)(a << 6 & 0xC0 | b & 0x3F);
            if (leftover != -1) {
                buffer[offset++] = (char)(leftover << 8 | current & 0xFF);
                leftover = -1;
                continue;
            }
            leftover = current & 0xFF;
        }
        return offset;
    }

    static {
        int i;
        pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', ','};
        pem_convert_array = new byte[256];
        for (i = 0; i < 255; ++i) {
            BASE64MailboxDecoder.pem_convert_array[i] = -1;
        }
        for (i = 0; i < pem_array.length; ++i) {
            BASE64MailboxDecoder.pem_convert_array[BASE64MailboxDecoder.pem_array[i]] = (byte)i;
        }
    }
}

