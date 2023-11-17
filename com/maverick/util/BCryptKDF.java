/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.BCrypt;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BCryptKDF {
    static final int BCRYPT_WORDS = 8;
    static final int BCRYPT_HASHSIZE = 32;

    static byte[] bcrypt_hash(byte[] sha2pass, byte[] sha2salt) {
        int i;
        byte[] ciphertext;
        BCrypt B = new BCrypt();
        byte[] out = new byte[32];
        try {
            ciphertext = "OxychromaticBlowfishSwatDynamite".getBytes("ASCII");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("ASCII not supported :\\");
        }
        int[] cdata = new int[8];
        B.init_key();
        B.ekskey(sha2salt, sha2pass);
        for (i = 0; i < 64; ++i) {
            B.key(sha2salt);
            B.key(sha2pass);
        }
        int[] j = new int[]{0};
        for (i = 0; i < 8; ++i) {
            cdata[i] = BCrypt.streamtoword(ciphertext, j);
        }
        for (i = 0; i < 64; ++i) {
            B.blf_enc(cdata, 4);
        }
        for (i = 0; i < cdata.length; ++i) {
            out[4 * i + 3] = (byte)(cdata[i] >> 24 & 0xFF);
            out[4 * i + 2] = (byte)(cdata[i] >> 16 & 0xFF);
            out[4 * i + 1] = (byte)(cdata[i] >> 8 & 0xFF);
            out[4 * i + 0] = (byte)(cdata[i] & 0xFF);
        }
        return out;
    }

    public static byte[] bcrypt_pbkdf(byte[] pass, byte[] salt, int keylen, int rounds) throws NoSuchAlgorithmException {
        byte[] out = new byte[32];
        byte[] tmpout = new byte[32];
        byte[] countsalt = new byte[4];
        byte[] key = new byte[keylen];
        int origkeylen = keylen;
        if (rounds < 1) {
            throw new IllegalArgumentException("Not enough rounds.");
        }
        if (pass.length == 0 || salt.length == 0 || keylen == 0 || keylen > out.length * out.length) {
            throw new IllegalArgumentException("Invalid pass, salt or key.");
        }
        int stride = (keylen + out.length - 1) / out.length;
        int amt = (keylen + stride - 1) / stride;
        MessageDigest ctx = MessageDigest.getInstance("SHA-512");
        ctx.update(pass);
        byte[] sha2pass = ctx.digest();
        int count = 1;
        while (keylen > 0) {
            int dest;
            int i;
            countsalt[0] = (byte)(count >> 24 & 0xFF);
            countsalt[1] = (byte)(count >> 16 & 0xFF);
            countsalt[2] = (byte)(count >> 8 & 0xFF);
            countsalt[3] = (byte)(count & 0xFF);
            ctx.reset();
            ctx.update(salt);
            ctx.update(countsalt);
            byte[] sha2salt = ctx.digest();
            tmpout = BCryptKDF.bcrypt_hash(sha2pass, sha2salt);
            System.arraycopy(tmpout, 0, out, 0, out.length);
            for (i = 1; i < rounds; ++i) {
                ctx.reset();
                ctx.update(tmpout);
                sha2salt = ctx.digest();
                tmpout = BCryptKDF.bcrypt_hash(sha2pass, sha2salt);
                for (int j = 0; j < out.length; ++j) {
                    int n = j;
                    out[n] = (byte)(out[n] ^ tmpout[j]);
                }
            }
            amt = Math.min(amt, keylen);
            for (i = 0; i < amt && (dest = i * stride + (count - 1)) < origkeylen; ++i) {
                key[dest] = out[i];
            }
            keylen -= i;
            ++count;
        }
        return key;
    }
}

