/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.MD5;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.NqException;
import java.util.Arrays;

public class HMACMD5
extends MD5 {
    private static final int HMAC_MAX_MD_CBLOCK = 64;

    public static void cmHMACMD5(byte[] key, int key_len, byte[] data, int data_len, byte[] md) throws NqException {
        Blob[] fragments = new Blob[]{new Blob()};
        fragments[0].data = new byte[data_len];
        fragments[0].len = data_len;
        System.arraycopy(data, 0, fragments[0].data, 0, data_len);
        Blob keyBlob = new Blob(key);
        HMACMD5.hmacmd5Internal(keyBlob, null, fragments, 1, md, 16);
    }

    public static void generateExtSecuritySessionKey(byte[] v2hash, byte[] encrypted, byte[] out) throws NqException {
        Blob key = new Blob();
        Blob[] fragment = new Blob[]{new Blob()};
        key.data = v2hash;
        key.len = 16;
        fragment[0].data = encrypted;
        fragment[0].len = 16;
        HMACMD5.hmacmd5Internal(key, null, fragment, 1, out, 16);
    }

    public static void hmacmd5Internal(Blob key, Blob key1, Blob[] dataFragments, int numFragments, byte[] buffer, int bufferSize) throws NqException {
        HMAC_MD5Context ctx = new HMAC_MD5Context();
        HMACMD5.HMACMD5_Init(ctx, key.data, key.len);
        for (int i = 0; i < numFragments; ++i) {
            if (dataFragments[i].data == null || dataFragments[i].len <= 0) continue;
            HMACMD5.HMACMD5_Update(ctx, dataFragments[i].data, dataFragments[i].len);
        }
        HMACMD5.HMACMD5_Final(ctx, buffer);
    }

    private static void HMACMD5_Init(HMAC_MD5Context ctx, byte[] key, int len) throws NqException {
        int i;
        Arrays.fill(ctx.iPad, (byte)0);
        Arrays.fill(ctx.oPad, (byte)0);
        for (i = 0; i < len; ++i) {
            ctx.iPad[i] = key[i];
            ctx.oPad[i] = key[i];
        }
        i = 0;
        while (i < 64) {
            int n = i;
            ctx.iPad[n] = (byte)(ctx.iPad[n] ^ 0x36);
            int n2 = i++;
            ctx.oPad[n2] = (byte)(ctx.oPad[n2] ^ 0x5C);
        }
        HMACMD5.MD5_Init(ctx.md5);
        HMACMD5.MD5_Update(ctx.md5, ctx.iPad, 64);
    }

    private static void HMACMD5_Update(HMAC_MD5Context ctx, byte[] data, int len) throws NqException {
        HMACMD5.MD5_Update(ctx.md5, data, len);
    }

    private static void HMACMD5_Final(HMAC_MD5Context ctx, byte[] md) throws NqException {
        MD5.MD5Context tempCtx = new MD5.MD5Context();
        HMACMD5.MD5_Final(ctx.md5, md);
        HMACMD5.MD5_Init(tempCtx);
        HMACMD5.MD5_Update(tempCtx, ctx.oPad, 64);
        HMACMD5.MD5_Update(tempCtx, md, 16);
        HMACMD5.MD5_Final(tempCtx, md);
    }

    static class HMAC_MD5Context {
        private static final int HMAC_MAX_MD_CBLOCK = 64;
        MD5.MD5Context md5 = new MD5.MD5Context();
        byte[] iPad = new byte[65];
        byte[] oPad = new byte[65];
    }
}

