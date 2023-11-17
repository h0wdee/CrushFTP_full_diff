/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.HMACMD5;
import com.visuality.nq.auth.MD4;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Crypt {
    private static SecureRandom randObj = new SecureRandom();
    public static final int AM_CRYPTER_NONE = 0;
    public static final int AM_CRYPTER_LM = 1;
    public static final int AM_CRYPTER_NTLM = 2;
    public static final int AM_CRYPTER_LM2 = 3;
    public static final int AM_CRYPTER_NTLM2 = 4;
    private static final byte[] perm1 = new byte[]{57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4};
    private static final byte[] perm2 = new byte[]{14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32};
    private static final byte[] perm3 = new byte[]{58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7};
    private static final byte[] perm4 = new byte[]{32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1};
    private static final byte[] perm5 = new byte[]{16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25};
    private static final byte[] perm6 = new byte[]{40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25};
    private static final byte[] sc = new byte[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
    private static final byte[][][] sbox = new byte[][][]{new byte[][]{{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7}, {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8}, {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0}, {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}}, new byte[][]{{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10}, {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5}, {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15}, {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}}, new byte[][]{{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8}, {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1}, {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7}, {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}}, new byte[][]{{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15}, {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9}, {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4}, {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}}, new byte[][]{{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9}, {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6}, {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14}, {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}}, new byte[][]{{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11}, {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8}, {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6}, {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}}, new byte[][]{{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1}, {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6}, {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2}, {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}}, new byte[][]{{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7}, {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2}, {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8}, {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}}};
    Blob pass1 = new Blob();
    Blob pass2 = new Blob();
    Blob macKey = new Blob();
    Blob response = new Blob();

    static int getPassLenByCrypter(int crypter) {
        int result = 0;
        switch (crypter) {
            case 0: {
                result = 0;
                break;
            }
            case 1: {
                result = 24;
                break;
            }
            case 3: {
                result = 24;
                break;
            }
            case 2: {
                result = 24;
                break;
            }
            case 4: {
                result = 1752;
                break;
            }
            default: {
                result = 0;
            }
        }
        return result;
    }

    static void createCrypt(int crypt1, int crypt2, Crypt crypt) {
        crypt.pass1 = new Blob(Crypt.getPassLenByCrypter(crypt1));
        crypt.pass2 = new Blob(Crypt.getPassLenByCrypter(crypt2));
        crypt.macKey = new Blob(16);
        crypt.response = new Blob(crypt.pass2.len == 0 ? (crypt.pass1.len == 0 ? 0 : crypt.pass1.len) : crypt.pass2.len);
        if (null != crypt.macKey.data) {
            Arrays.fill(crypt.macKey.data, (byte)0);
        }
        if (null != crypt.response.data) {
            Arrays.fill(crypt.response.data, (byte)0);
        }
    }

    static void createContext(Context context, Credentials credentials, byte[] key, Blob names, int[] timeStamp) throws NqException {
        context.credentials = credentials;
        context.key = key;
        context.names = names;
        context.timeStamp = timeStamp;
        byte[] passwordA = new String(((PasswordCredentials)credentials).getPassword()).getBytes();
        if (null == passwordA) {
            return;
        }
        Crypt.hashPassword(passwordA, context.lmHash);
        try {
            MD4.cmMD4(context.ntlmHash, ((PasswordCredentials)credentials).getPassword().getBytes("UTF-16LE"), ((PasswordCredentials)credentials).getPassword().length() * 2);
        }
        catch (UnsupportedEncodingException e) {
            throw new NqException(e.getMessage(), -22);
        }
        passwordA = null;
    }

    public static boolean cryptEncrypt(Credentials credentials, int crypt1, int crypt2, byte[] encryptionKey, Blob names, int[] timeStamp, Crypt crypt) throws NqException {
        Context context = new Context();
        boolean result = false;
        Crypt.createCrypt(crypt1, crypt2, crypt);
        Crypt.createContext(context, credentials, encryptionKey, names, timeStamp);
        if (!Crypt.cryptByCrypter(crypt1, context, crypt)) {
            return result;
        }
        if (!Crypt.cryptByCrypter(crypt2, context, crypt)) {
            return result;
        }
        result = true;
        return result;
    }

    public static boolean cryptByCrypter(int crypt1, Context context, Crypt crypt) throws NqException {
        boolean result = false;
        switch (crypt1) {
            case 0: {
                result = true;
                break;
            }
            case 1: {
                result = Crypt.encryptLM(context, crypt);
                break;
            }
            case 3: {
                result = Crypt.encryptLMv2(context, crypt);
                break;
            }
            case 2: {
                result = Crypt.encryptNTLM(context, crypt);
                break;
            }
            case 4: {
                result = Crypt.encryptNTLMv2(context, crypt);
                break;
            }
        }
        return result;
    }

    private static boolean encryptLM(Context context, Crypt crypt) {
        crypt.pass1.len = 24;
        if (null == crypt.pass1.data || null == crypt.macKey.data || null == crypt.response.data) {
            return false;
        }
        crypt.pass1.len = Crypt.encryptNTLMPassword(context.key, context.lmHash, crypt.pass1.data);
        Arrays.fill(crypt.macKey.data, (byte)0);
        System.arraycopy(crypt.pass1.data, 0, crypt.macKey.data, 0, 8);
        crypt.macKey.len = 8;
        System.arraycopy(crypt.pass1.data, 0, crypt.response.data, 0, 24);
        return true;
    }

    private static boolean encryptLMv2(Context context, Crypt crypt) throws NqException {
        byte[] v2hash = new byte[16];
        byte[] blip = new byte[8];
        byte[] data = new byte[16];
        byte[] hmac = new byte[16];
        boolean result = false;
        randObj.nextBytes(blip);
        BufferWriter writer = new BufferWriter(data, 0, false);
        writer.writeBytes(context.key, 8);
        writer.writeBytes(blip, 8);
        if (null == crypt.pass1.data || null == crypt.macKey.data || null == crypt.response.data) {
            return result;
        }
        Crypt.v2Hash(context, v2hash);
        HMACMD5.cmHMACMD5(v2hash, v2hash.length, data, data.length, hmac);
        crypt.pass1.data = new byte[hmac.length + blip.length];
        for (int i = 0; i < hmac.length + blip.length; ++i) {
            crypt.pass1.data[i] = i < hmac.length ? hmac[i] : blip[i - hmac.length];
        }
        crypt.pass1.len = hmac.length + blip.length;
        HMACMD5.cmHMACMD5(v2hash, v2hash.length, hmac, hmac.length, crypt.macKey.data);
        crypt.response.data = new byte[24];
        System.arraycopy(crypt.pass1.data, 0, crypt.response.data, 0, crypt.response.data.length);
        return true;
    }

    private static boolean encryptNTLM(Context context, Crypt crypt) {
        if (null == crypt.pass2.data || null == crypt.macKey.data || null == crypt.response.data) {
            return false;
        }
        short passLen = Crypt.encryptNTLMPassword(context.key, context.ntlmHash, crypt.pass2.data);
        crypt.pass2.len = passLen;
        MD4.cmMD4(crypt.macKey.data, context.ntlmHash, 16);
        crypt.macKey.len = 16;
        System.arraycopy(crypt.pass2.data, 0, crypt.response.data, 0, 24);
        return true;
    }

    private static boolean encryptNTLMv2(Context context, Crypt crypt) throws NqException {
        byte[] v2Hash = new byte[16];
        byte[] hmac = new byte[16];
        Blob blob = new Blob();
        blob.len = crypt.pass2.len - 16;
        blob.data = new byte[blob.len];
        byte[] data = new byte[crypt.pass2.len - 8];
        Crypt.createNTLMv2Blob(context, blob);
        BufferWriter writer = new BufferWriter(data, 0, false);
        writer.writeBytes(context.key, 8);
        writer.writeBytes(blob.data, blob.len);
        Crypt.v2Hash(context, v2Hash);
        HMACMD5.cmHMACMD5(v2Hash, v2Hash.length, data, 8 + blob.len, hmac);
        writer = new BufferWriter(crypt.pass2.data, 0, false);
        writer.writeBytes(hmac, hmac.length);
        writer.writeBytes(blob.data, blob.data.length);
        HMACMD5.cmHMACMD5(v2Hash, v2Hash.length, hmac, hmac.length, crypt.macKey.data);
        crypt.response.len = crypt.pass2.len = hmac.length + blob.len;
        crypt.response.data = new byte[crypt.response.len];
        System.arraycopy(crypt.pass2.data, 0, crypt.response.data, 0, crypt.response.len);
        return true;
    }

    private static void createNTLMv2Blob(Context context, Blob blob) {
        BufferWriter writer = new BufferWriter(blob.data, 0, false);
        writer.writeInt4(257);
        writer.writeInt4(0);
        writer.writeInt4(context.getTimeStamp()[0]);
        writer.writeInt4(context.getTimeStamp()[1]);
        blob.len = 16;
        byte[] random = new byte[8];
        randObj.nextBytes(random);
        writer.writeBytes(random, 8);
        writer.writeInt4(0);
        blob.len += 12;
        if (null != context.names) {
            writer.writeBytes(context.names.data, context.names.len);
            blob.len += context.names.len;
            return;
        }
        blob.len += 8;
    }

    private static boolean v2Hash(Context context, byte[] v2Hash) throws NqException {
        boolean result = false;
        String name = new String(((PasswordCredentials)context.credentials).getUser()).toUpperCase();
        if (!new String(((PasswordCredentials)context.credentials).getUser()).contains("@")) {
            name = name + new String(context.credentials.getDomain()).toUpperCase();
        }
        try {
            byte[] data = name.getBytes("UTF-16LE");
            HMACMD5.cmHMACMD5(context.ntlmHash, 16, data, data.length, v2Hash);
        }
        catch (UnsupportedEncodingException e) {
            TraceLog.get().error("UTF-16LE is not supported by the platform", 5, 0);
        }
        result = true;
        return result;
    }

    public static short encryptNTLMPassword(byte[] key, byte[] password, byte[] encrypted) {
        Crypt.encryptHashedPassword(password, key, encrypted);
        return 24;
    }

    private static void encryptHashedPassword(byte[] hashed, byte[] key, byte[] encrypted) {
        byte[] hshPasswdBuf = new byte[21];
        System.arraycopy(hashed, 0, hshPasswdBuf, 0, 16);
        Crypt.E_P24(hshPasswdBuf, key, encrypted);
    }

    private static byte[] hashPassword(byte[] password, byte[] hashed) {
        byte[] passwdBuf = new byte[15];
        byte[] hshPasswdBuf = new byte[21];
        System.arraycopy(password, 0, passwdBuf, 0, password.length < passwdBuf.length ? password.length : passwdBuf.length);
        for (int i = 0; i < passwdBuf.length; ++i) {
            passwdBuf[i] = (byte)Character.toUpperCase(passwdBuf[i]);
        }
        Crypt.E_P16(passwdBuf, hshPasswdBuf);
        System.arraycopy(hshPasswdBuf, 0, hashed, 0, hashed.length);
        return hashed;
    }

    private static void E_P16(byte[] p14, byte[] p16) {
        byte[] sp8 = new byte[]{75, 71, 83, 33, 64, 35, 36, 37};
        Crypt.cryptHash(p16, 0, sp8, p14, 0, 1);
        Crypt.cryptHash(p16, 8, sp8, p14, 7, 1);
    }

    private static void E_P24(byte[] p21, byte[] c8, byte[] p24) {
        Crypt.cryptHash(p24, 0, c8, p21, 0, 1);
        Crypt.cryptHash(p24, 8, c8, p21, 7, 1);
        Crypt.cryptHash(p24, 16, c8, p21, 14, 1);
    }

    private static void cryptHash(byte[] out, int outIndex, byte[] in, byte[] key, int keyIndex, int forw) {
        int i;
        byte[] outb = new byte[64];
        byte[] inb = new byte[64];
        byte[] keyb = new byte[64];
        byte[] key2 = new byte[8];
        Crypt.str_to_key(key, keyIndex, key2);
        for (i = 0; i < 64; ++i) {
            inb[i] = (byte)(0 != (in[i / 8] & 1 << 7 - i % 8) ? 1 : 0);
            keyb[i] = (byte)(0 != (key2[i / 8] & 1 << 7 - i % 8) ? 1 : 0);
            outb[i] = 0;
        }
        Crypt.dohash(outb, inb, keyb, forw);
        for (i = 0; i < 8; ++i) {
            out[i + outIndex] = 0;
        }
        for (i = 0; i < 64; ++i) {
            if (0 == outb[i]) continue;
            int n = i / 8 + outIndex;
            out[n] = (byte)(out[n] | (byte)(1 << 7 - i % 8));
        }
    }

    private static void str_to_key(byte[] str, int strIndex, byte[] key) {
        key[0] = (byte)(str[0 + strIndex] >>> 1);
        key[1] = (byte)((byte)((str[0 + strIndex] & 1) << 6) | (byte)((0xFF & str[1 + strIndex]) >>> 2));
        key[2] = (byte)((byte)((str[1 + strIndex] & 3) << 5) | (byte)((0xFF & str[2 + strIndex]) >>> 3));
        key[3] = (byte)((byte)((str[2 + strIndex] & 7) << 4) | (byte)((0xFF & str[3 + strIndex]) >>> 4));
        key[4] = (byte)((byte)((str[3 + strIndex] & 0xF) << 3) | (byte)((0xFF & str[4 + strIndex]) >>> 5));
        key[5] = (byte)((byte)((str[4 + strIndex] & 0x1F) << 2) | (byte)((0xFF & str[5 + strIndex]) >>> 6));
        key[6] = (byte)((byte)((str[5 + strIndex] & 0x3F) << 1) | (byte)((0xFF & str[6 + strIndex]) >>> 7));
        key[7] = (byte)(str[6 + strIndex] & 0x7F);
        for (int i = 0; i < 8; ++i) {
            key[i] = (byte)(key[i] << 1);
        }
    }

    private static void dohash(byte[] out, byte[] in, byte[] key, int forw) {
        int j;
        int i;
        byte[] pk1 = new byte[56];
        byte[] c = new byte[28];
        byte[] d = new byte[28];
        byte[] cd = new byte[56];
        byte[][] ki = new byte[16][48];
        byte[] pd1 = new byte[64];
        byte[] l = new byte[32];
        byte[] r = new byte[32];
        byte[] rl = new byte[64];
        Crypt.permute(pk1, key, perm1, 56);
        for (i = 0; i < 28; ++i) {
            c[i] = pk1[i];
        }
        for (i = 0; i < 28; ++i) {
            d[i] = pk1[i + 28];
        }
        for (i = 0; i < 16; ++i) {
            Crypt.lshift(c, sc[i], 28);
            Crypt.lshift(d, sc[i], 28);
            Crypt.concat(cd, c, d, 28, 28);
            Crypt.permute(ki[i], cd, perm2, 48);
        }
        Crypt.permute(pd1, in, perm3, 64);
        for (j = 0; j < 32; ++j) {
            l[j] = pd1[j];
            r[j] = pd1[j + 32];
        }
        for (i = 0; i < 16; ++i) {
            int k;
            byte[] er = new byte[48];
            byte[] erk = new byte[48];
            byte[][] b = new byte[8][6];
            byte[] cBlock = new byte[32];
            byte[] pcb = new byte[32];
            byte[] r2 = new byte[32];
            Crypt.permute(er, r, perm4, 48);
            Crypt.xorArray(erk, er, ki[0 != forw ? i : 15 - i], 48);
            for (j = 0; j < 8; ++j) {
                for (k = 0; k < 6; ++k) {
                    b[j][k] = erk[j * 6 + k];
                }
            }
            for (j = 0; j < 8; ++j) {
                int m = b[j][0] << 1 | b[j][5];
                int n = b[j][1] << 3 | b[j][2] << 2 | b[j][3] << 1 | b[j][4];
                for (k = 0; k < 4; ++k) {
                    b[j][k] = (byte)(0 != (sbox[j][m][n] & 1 << 3 - k) ? 1 : 0);
                }
            }
            for (j = 0; j < 8; ++j) {
                for (k = 0; k < 4; ++k) {
                    cBlock[j * 4 + k] = b[j][k];
                }
            }
            Crypt.permute(pcb, cBlock, perm5, 32);
            Crypt.xorArray(r2, l, pcb, 32);
            for (j = 0; j < 32; ++j) {
                l[j] = r[j];
                r[j] = r2[j];
            }
        }
        Crypt.concat(rl, r, l, 32, 32);
        Crypt.permute(out, rl, perm6, 64);
    }

    private static void permute(byte[] out, byte[] in, byte[] p, int n) {
        for (int i = 0; i < n; ++i) {
            out[i] = in[p[i] - 1];
        }
    }

    private static void lshift(byte[] d, int count, int n) {
        int i;
        byte[] out = new byte[64];
        for (i = 0; i < n; ++i) {
            out[i] = d[(i + count) % n];
        }
        for (i = 0; i < n; ++i) {
            d[i] = out[i];
        }
    }

    private static void concat(byte[] out, byte[] in1, byte[] in2, int l1, int l2) {
        int i;
        for (i = 0; i < l1; ++i) {
            out[i] = in1[i];
        }
        while (i < l1 + l2) {
            out[i] = in2[i - l1];
            ++i;
        }
    }

    private static void xorArray(byte[] out, byte[] in1, byte[] in2, int n) {
        for (int i = 0; i < n; ++i) {
            out[i] = (byte)(in1[i] ^ in2[i]);
        }
    }

    static class Context {
        byte[] key;
        Credentials credentials;
        Blob names;
        byte[] lmHash;
        byte[] ntlmHash;
        int[] timeStamp = new int[2];

        public int[] getTimeStamp() {
            return this.timeStamp;
        }

        public Context() {
            this.lmHash = new byte[16];
            this.ntlmHash = new byte[16];
        }
    }
}

