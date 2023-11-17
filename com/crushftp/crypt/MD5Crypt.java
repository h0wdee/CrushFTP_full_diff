/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public final class MD5Crypt {
    private static final String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String to64(long v, int size) {
        StringBuffer result = new StringBuffer();
        while (--size >= 0) {
            result.append(itoa64.charAt((int)(v & 0x3FL)));
            v >>>= 6;
        }
        return result.toString();
    }

    private static final void clearbits(byte[] bits) {
        int i = 0;
        while (i < bits.length) {
            bits[i] = 0;
            ++i;
        }
    }

    private static final int bytes2u(byte inp) {
        return inp & 0xFF;
    }

    private static MessageDigest getMD5() {
        try {
            return MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static final String crypt(String password) {
        StringBuffer salt = new StringBuffer();
        Random randgen = new Random();
        while (salt.length() < 8) {
            int index = (int)(randgen.nextFloat() * (float)SALTCHARS.length());
            salt.append(SALTCHARS.substring(index, index + 1));
        }
        return MD5Crypt.crypt(password, salt.toString());
    }

    public static final String crypt(String password, String salt) {
        return MD5Crypt.crypt(password, salt, "$1$");
    }

    public static final String apacheCrypt(String password) {
        StringBuffer salt = new StringBuffer();
        Random randgen = new Random();
        while (salt.length() < 8) {
            int index = (int)(randgen.nextFloat() * (float)SALTCHARS.length());
            salt.append(SALTCHARS.substring(index, index + 1));
        }
        return MD5Crypt.apacheCrypt(password, salt.toString());
    }

    public static final String apacheCrypt(String password, String salt) {
        return MD5Crypt.crypt(password, salt, "$apr1$");
    }

    public static final String crypt(String password, String salt, String magic) {
        if (salt.startsWith(magic)) {
            salt = salt.substring(magic.length());
        }
        if (salt.indexOf(36) != -1) {
            salt = salt.substring(0, salt.indexOf(36));
        }
        if (salt.length() > 8) {
            salt = salt.substring(0, 8);
        }
        MessageDigest ctx = MD5Crypt.getMD5();
        ctx.update(password.getBytes());
        ctx.update(magic.getBytes());
        ctx.update(salt.getBytes());
        MessageDigest ctx1 = MD5Crypt.getMD5();
        ctx1.update(password.getBytes());
        ctx1.update(salt.getBytes());
        ctx1.update(password.getBytes());
        byte[] finalState = ctx1.digest();
        int pl = password.length();
        while (pl > 0) {
            ctx.update(finalState, 0, pl > 16 ? 16 : pl);
            pl -= 16;
        }
        MD5Crypt.clearbits(finalState);
        int i = password.length();
        while (i != 0) {
            if ((i & 1) != 0) {
                ctx.update(finalState, 0, 1);
            } else {
                ctx.update(password.getBytes(), 0, 1);
            }
            i >>>= 1;
        }
        finalState = ctx.digest();
        i = 0;
        while (i < 1000) {
            ctx1.reset();
            if ((i & 1) != 0) {
                ctx1.update(password.getBytes());
            } else {
                ctx1.update(finalState, 0, 16);
            }
            if (i % 3 != 0) {
                ctx1.update(salt.getBytes());
            }
            if (i % 7 != 0) {
                ctx1.update(password.getBytes());
            }
            if ((i & 1) != 0) {
                ctx1.update(finalState, 0, 16);
            } else {
                ctx1.update(password.getBytes());
            }
            finalState = ctx1.digest();
            ++i;
        }
        StringBuffer result = new StringBuffer();
        result.append(magic);
        result.append(salt);
        result.append("$");
        long l = MD5Crypt.bytes2u(finalState[0]) << 16 | MD5Crypt.bytes2u(finalState[6]) << 8 | MD5Crypt.bytes2u(finalState[12]);
        result.append(MD5Crypt.to64(l, 4));
        l = MD5Crypt.bytes2u(finalState[1]) << 16 | MD5Crypt.bytes2u(finalState[7]) << 8 | MD5Crypt.bytes2u(finalState[13]);
        result.append(MD5Crypt.to64(l, 4));
        l = MD5Crypt.bytes2u(finalState[2]) << 16 | MD5Crypt.bytes2u(finalState[8]) << 8 | MD5Crypt.bytes2u(finalState[14]);
        result.append(MD5Crypt.to64(l, 4));
        l = MD5Crypt.bytes2u(finalState[3]) << 16 | MD5Crypt.bytes2u(finalState[9]) << 8 | MD5Crypt.bytes2u(finalState[15]);
        result.append(MD5Crypt.to64(l, 4));
        l = MD5Crypt.bytes2u(finalState[4]) << 16 | MD5Crypt.bytes2u(finalState[10]) << 8 | MD5Crypt.bytes2u(finalState[5]);
        result.append(MD5Crypt.to64(l, 4));
        l = MD5Crypt.bytes2u(finalState[11]);
        result.append(MD5Crypt.to64(l, 2));
        MD5Crypt.clearbits(finalState);
        return result.toString();
    }

    public static final boolean verifyPassword(String plaintextPass, String md5CryptText) {
        if (md5CryptText.startsWith("$1$")) {
            return md5CryptText.equals(MD5Crypt.crypt(plaintextPass, md5CryptText));
        }
        if (md5CryptText.startsWith("$apr1$")) {
            return md5CryptText.equals(MD5Crypt.apacheCrypt(plaintextPass, md5CryptText));
        }
        throw new RuntimeException("Bad md5CryptText");
    }
}

