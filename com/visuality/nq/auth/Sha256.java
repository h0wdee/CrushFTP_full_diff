/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.common.Blob;
import java.util.Arrays;

public class Sha256 {
    public static final int SHA256_DIGEST_SIZE = 32;
    public static final int SHA256_BLOCK_SIZE = 64;
    public static final int SHA256_SECURITY_SIGNATURE_SIZE = 16;
    private static final int[] sha256_h0 = new int[]{1779033703, -1150833019, 1013904242, -1521486534, 1359893119, -1694144372, 528734635, 1541459225};
    private static final int[] sha256_k = new int[]{1116352408, 1899447441, -1245643825, -373957723, 961987163, 1508970993, -1841331548, -1424204075, -670586216, 310598401, 607225278, 1426881987, 1925078388, -2132889090, -1680079193, -1046744716, -459576895, -272742522, 264347078, 604807628, 770255983, 1249150122, 1555081692, 1996064986, -1740746414, -1473132947, -1341970488, -1084653625, -958395405, -710438585, 113926993, 338241895, 666307205, 773529912, 1294757372, 1396182291, 1695183700, 1986661051, -2117940946, -1838011259, -1564481375, -1474664885, -1035236496, -949202525, -778901479, -694614492, -200395387, 275423344, 430227734, 506948616, 659060556, 883997877, 958139571, 1322822218, 1537002063, 1747873779, 1955562222, 2024104815, -2067236844, -1933114872, -1866530822, -1538233109, -1090935817, -965641998};
    private static final int sizeOfInt = 4;

    private static void sha256Transf(Sha256Ctx ctx, byte[] message, int block_nb) {
        int[] w = new int[64];
        int[] wv = new int[8];
        byte[] sub_block = new byte[message.length];
        int j = 0;
        for (int i = 0; i < block_nb; ++i) {
            System.arraycopy(message, i << 6, sub_block, 0, message.length - (i << 6));
            int tmp = 0;
            for (j = 0; j < 16; ++j) {
                tmp = j << 2;
                w[j] = 0xFF & sub_block[tmp + 3] | 0xFF00 & sub_block[tmp + 2] << 8 | 0xFF0000 & sub_block[tmp + 1] << 16 | 0xFF000000 & sub_block[tmp + 0] << 24;
            }
            for (j = 16; j < 64; ++j) {
                w[j] = ((w[j - 2] >>> 17 | w[j - 2] << 15) ^ (w[j - 2] >>> 19 | w[j - 2] << 13) ^ w[j - 2] >>> 10) + w[j - 7] + ((w[j - 15] >>> 7 | w[j - 15] << 25) ^ (w[j - 15] >>> 18 | w[j - 15] << 14) ^ w[j - 15] >>> 3) + w[j - 16];
            }
            for (j = 0; j < 8; ++j) {
                wv[j] = ctx.h[j];
            }
            for (j = 0; j < 64; ++j) {
                int t1 = wv[7] + ((wv[4] >>> 6 | wv[4] << 26) ^ (wv[4] >>> 11 | wv[4] << 21) ^ (wv[4] >>> 25 | wv[4] << 7)) + (wv[4] & wv[5] ^ ~wv[4] & wv[6]) + sha256_k[j] + w[j];
                int t2 = ((wv[0] >>> 2 | wv[0] << 30) ^ (wv[0] >>> 13 | wv[0] << 19) ^ (wv[0] >>> 22 | wv[0] << 10)) + (wv[0] & wv[1] ^ wv[0] & wv[2] ^ wv[1] & wv[2]);
                wv[7] = wv[6];
                wv[6] = wv[5];
                wv[5] = wv[4];
                wv[4] = wv[3] + t1;
                wv[3] = wv[2];
                wv[2] = wv[1];
                wv[1] = wv[0];
                wv[0] = t1 + t2;
            }
            for (j = 0; j < 8; ++j) {
                int n = j;
                ctx.h[n] = ctx.h[n] + wv[j];
            }
        }
    }

    private static void sha256Final(Sha256Ctx ctx, byte[] digest) {
        int i;
        int block_nb = 1;
        if (55 < ctx.len % 64) {
            block_nb = 2;
        }
        int len_b = ctx.tot_len + ctx.len << 3;
        int pm_len = block_nb << 6;
        for (i = 0; i < pm_len - ctx.len; ++i) {
            ctx.block[ctx.len + i] = 0;
        }
        ctx.block[ctx.len] = -128;
        ctx.block[pm_len - 4 + 3] = (byte)len_b;
        ctx.block[pm_len - 4 + 2] = (byte)(len_b >>> 8);
        ctx.block[pm_len - 4 + 1] = (byte)(len_b >>> 16);
        ctx.block[pm_len - 4] = (byte)(len_b >>> 24);
        Sha256.sha256Transf(ctx, ctx.block, block_nb);
        for (i = 0; i < 8; ++i) {
            digest[(i << 2) + 3] = (byte)ctx.h[i];
            digest[(i << 2) + 2] = (byte)(ctx.h[i] >>> 8);
            digest[(i << 2) + 1] = (byte)(ctx.h[i] >>> 16);
            digest[i << 2] = (byte)(ctx.h[i] >>> 24);
        }
    }

    private static void sha256Update(Sha256Ctx ctx, byte[] message, int len) {
        int i;
        int tmp_len = 64 - ctx.len;
        int rem_len = len < tmp_len ? len : tmp_len;
        for (i = 0; i < (rem_len < message.length ? rem_len : message.length); ++i) {
            ctx.block[ctx.len + i] = message[i];
        }
        if (ctx.len + len < 64) {
            ctx.len += len;
            return;
        }
        int new_len = len - rem_len;
        int block_nb = new_len / 64;
        byte[] shifted_message = new byte[message.length - rem_len];
        System.arraycopy(message, rem_len, shifted_message, 0, shifted_message.length);
        Sha256.sha256Transf(ctx, ctx.block, 1);
        Sha256.sha256Transf(ctx, shifted_message, block_nb);
        rem_len = new_len % 64;
        int j = block_nb << 6;
        for (i = 0; i < rem_len; ++i) {
            ctx.block[i] = shifted_message[j + i];
        }
        ctx.len = rem_len;
        ctx.tot_len += block_nb + 1 << 6;
    }

    public static void sha256Internal(Blob key, Blob key1, Blob[] dataFragments, int numFragments, byte[] buffer, int bufferSize) {
        Sha256Ctx ctx = new Sha256().new Sha256Ctx();
        for (int i = 0; i < numFragments; ++i) {
            if (null == dataFragments[i].data || dataFragments[i].len <= 0) continue;
            Sha256.sha256Update(ctx, dataFragments[i].data, dataFragments[i].len);
        }
        Sha256.sha256Final(ctx, buffer);
    }

    public static void keyDerivation(byte[] key, int keyLen, byte[] label, int labelLen, byte[] context, int contextLen, byte[] derivedKey) {
        int i;
        int tmp;
        byte[] temp1 = new byte[]{0, 0, 0, 1};
        byte[] temp2 = new byte[]{0, 0, 0, -128};
        byte[] zero = new byte[]{0};
        byte[] ipad = new byte[64];
        byte[] opad = new byte[64];
        byte[] digest = new byte[32];
        Blob[] fragments1 = new Blob[6];
        Blob[] fragments2 = new Blob[2];
        for (tmp = 0; tmp < 6; ++tmp) {
            fragments1[tmp] = new Blob();
        }
        for (tmp = 0; tmp < 2; ++tmp) {
            fragments2[tmp] = new Blob();
        }
        fragments1[0].data = ipad;
        fragments1[0].len = ipad.length;
        fragments1[1].data = temp1;
        fragments1[1].len = temp1.length;
        fragments1[2].data = label;
        fragments1[2].len = labelLen;
        fragments1[3].data = zero;
        fragments1[3].len = 1;
        fragments1[4].data = context;
        fragments1[4].len = contextLen;
        fragments1[5].data = temp2;
        fragments1[5].len = temp2.length;
        fragments2[0].data = opad;
        fragments2[0].len = opad.length;
        fragments2[1].data = digest;
        fragments2[1].len = digest.length;
        Arrays.fill(ipad, (byte)54);
        Arrays.fill(opad, (byte)92);
        for (i = 0; i < keyLen; ++i) {
            int n = i;
            ipad[n] = (byte)(ipad[n] ^ key[i]);
            int n2 = i;
            opad[n2] = (byte)(opad[n2] ^ key[i]);
        }
        Sha256.sha256Internal(null, null, fragments1, 6, digest, digest.length);
        Sha256.sha256Internal(null, null, fragments2, 2, digest, digest.length);
        for (i = 0; i < 16; ++i) {
            derivedKey[i] = digest[i];
        }
    }

    class Sha256Ctx {
        int tot_len;
        int len;
        byte[] block = new byte[128];
        int[] h = new int[8];

        Sha256Ctx() {
            for (int i = 0; i < 8; ++i) {
                this.h[i] = sha256_h0[i];
            }
            this.len = 0;
            this.tot_len = 0;
        }
    }
}

