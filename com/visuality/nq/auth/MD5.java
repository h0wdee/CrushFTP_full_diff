/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import java.util.Arrays;

public class MD5 {
    public static void cmMD5(byte[] out, byte[] in, int n) throws NqException {
        Blob[] fragment = new Blob[]{new Blob()};
        fragment[0].data = in;
        fragment[0].len = n;
        MD5.md5Internal(null, null, fragment, 1, out, 16);
    }

    public static void md5Internal(Blob key, Blob key1, Blob[] dataFragments, int numFragments, byte[] buffer, int bufferSize) throws NqException {
        MD5Context ctx = new MD5Context();
        MD5.MD5_Init(ctx);
        for (int i = 0; i < numFragments; ++i) {
            if (dataFragments[i].data == null || dataFragments[i].len <= 0) continue;
            MD5.MD5_Update(ctx, dataFragments[i].data, dataFragments[i].len);
        }
        MD5.MD5_Final(ctx, buffer);
    }

    static void MD5_Init(MD5Context ctx) {
        ctx.buf[0] = 1732584193;
        ctx.buf[1] = -271733879;
        ctx.buf[2] = -1732584194;
        ctx.buf[3] = 271733878;
        ctx.bits[0] = 0;
        ctx.bits[1] = 0;
    }

    static void MD5_Final(MD5Context ctx, byte[] digest) throws NqException {
        int count = ctx.bits[0] >>> 3 & 0x3F;
        BufferWriter p = new BufferWriter(ctx.in, count, false);
        p.writeByte((byte)-128);
        count = 63 - count;
        if (count < 8) {
            p.writeZeros(count);
            MD5.MD5_Transform(ctx.buf, ctx.in);
            Arrays.fill(ctx.in, 0, 56, (byte)0);
        } else {
            p.writeZeros(count - 8);
        }
        p.setOffset(56);
        p.writeInt4(ctx.bits[0]);
        p.writeInt4(ctx.bits[1]);
        MD5.MD5_Transform(ctx.buf, ctx.in);
        p = new BufferWriter(digest, 0, false);
        for (int i = 0; i < 4; ++i) {
            p.writeInt4(ctx.buf[i]);
        }
    }

    static void MD5_Update(MD5Context ctx, byte[] buf, int len) throws NqException {
        byte[] tmpBuff;
        int t = ctx.bits[0];
        ctx.bits[0] = t + (len << 3);
        if (ctx.bits[0] < t) {
            ctx.bits[1] = ctx.bits[1] + 1;
        }
        ctx.bits[1] = ctx.bits[1] + (len >>> 29);
        t = t >>> 3 & 0x3F;
        BufferWriter p = new BufferWriter(ctx.in, 0, false);
        int i = 0;
        if (0 != t) {
            p.setOffset(t);
            t = 64 - t;
            if (len < t) {
                p.writeBytes(buf, len);
                return;
            }
            p.writeBytes(buf, t);
            MD5.MD5_Transform(ctx.buf, ctx.in);
            i += t;
            len -= t;
        }
        while (len >= 64) {
            p.setOffset(0);
            tmpBuff = new byte[64];
            System.arraycopy(buf, i, tmpBuff, 0, tmpBuff.length);
            p.writeBytes(tmpBuff, 64);
            MD5.MD5_Transform(ctx.buf, ctx.in);
            i += 64;
            len -= 64;
        }
        p.setOffset(0);
        tmpBuff = new byte[len];
        System.arraycopy(buf, i, tmpBuff, 0, tmpBuff.length);
        p.writeBytes(tmpBuff, len);
    }

    static void MD5_Transform(int[] buf, byte[] inByte) throws NqException {
        int[] in = new int[inByte.length / 4];
        BufferReader br = new BufferReader(inByte, 0, false);
        for (int i = 0; i < in.length; ++i) {
            in[i] = br.readInt4();
        }
        int a = buf[0];
        int b = buf[1];
        int c = buf[2];
        int d = buf[3];
        a += (d ^ b & (c ^ d)) + in[0] + -680876936;
        a = a << 7 | a >>> 25;
        d += (c ^ (a += b) & (b ^ c)) + in[1] + -389564586;
        d = d << 12 | d >>> 20;
        c += (b ^ (d += a) & (a ^ b)) + in[2] + 606105819;
        c = c << 17 | c >>> 15;
        b += (a ^ (c += d) & (d ^ a)) + in[3] + -1044525330;
        b = b << 22 | b >>> 10;
        a += (d ^ (b += c) & (c ^ d)) + in[4] + -176418897;
        a = a << 7 | a >>> 25;
        d += (c ^ (a += b) & (b ^ c)) + in[5] + 1200080426;
        d = d << 12 | d >>> 20;
        c += (b ^ (d += a) & (a ^ b)) + in[6] + -1473231341;
        c = c << 17 | c >>> 15;
        b += (a ^ (c += d) & (d ^ a)) + in[7] + -45705983;
        b = b << 22 | b >>> 10;
        a += (d ^ (b += c) & (c ^ d)) + in[8] + 1770035416;
        a = a << 7 | a >>> 25;
        d += (c ^ (a += b) & (b ^ c)) + in[9] + -1958414417;
        d = d << 12 | d >>> 20;
        c += (b ^ (d += a) & (a ^ b)) + in[10] + -42063;
        c = c << 17 | c >>> 15;
        b += (a ^ (c += d) & (d ^ a)) + in[11] + -1990404162;
        b = b << 22 | b >>> 10;
        a += (d ^ (b += c) & (c ^ d)) + in[12] + 1804603682;
        a = a << 7 | a >>> 25;
        d += (c ^ (a += b) & (b ^ c)) + in[13] + -40341101;
        d = d << 12 | d >>> 20;
        c += (b ^ (d += a) & (a ^ b)) + in[14] + -1502002290;
        c = c << 17 | c >>> 15;
        b += (a ^ (c += d) & (d ^ a)) + in[15] + 1236535329;
        b = b << 22 | b >>> 10;
        a += (c ^ d & ((b += c) ^ c)) + in[1] + -165796510;
        a = a << 5 | a >>> 27;
        d += (b ^ c & ((a += b) ^ b)) + in[6] + -1069501632;
        d = d << 9 | d >>> 23;
        c += (a ^ b & ((d += a) ^ a)) + in[11] + 643717713;
        c = c << 14 | c >>> 18;
        b += (d ^ a & ((c += d) ^ d)) + in[0] + -373897302;
        b = b << 20 | b >>> 12;
        a += (c ^ d & ((b += c) ^ c)) + in[5] + -701558691;
        a = a << 5 | a >>> 27;
        d += (b ^ c & ((a += b) ^ b)) + in[10] + 38016083;
        d = d << 9 | d >>> 23;
        c += (a ^ b & ((d += a) ^ a)) + in[15] + -660478335;
        c = c << 14 | c >>> 18;
        b += (d ^ a & ((c += d) ^ d)) + in[4] + -405537848;
        b = b << 20 | b >>> 12;
        a += (c ^ d & ((b += c) ^ c)) + in[9] + 568446438;
        a = a << 5 | a >>> 27;
        d += (b ^ c & ((a += b) ^ b)) + in[14] + -1019803690;
        d = d << 9 | d >>> 23;
        c += (a ^ b & ((d += a) ^ a)) + in[3] + -187363961;
        c = c << 14 | c >>> 18;
        b += (d ^ a & ((c += d) ^ d)) + in[8] + 1163531501;
        b = b << 20 | b >>> 12;
        a += (c ^ d & ((b += c) ^ c)) + in[13] + -1444681467;
        a = a << 5 | a >>> 27;
        d += (b ^ c & ((a += b) ^ b)) + in[2] + -51403784;
        d = d << 9 | d >>> 23;
        c += (a ^ b & ((d += a) ^ a)) + in[7] + 1735328473;
        c = c << 14 | c >>> 18;
        b += (d ^ a & ((c += d) ^ d)) + in[12] + -1926607734;
        b = b << 20 | b >>> 12;
        a += ((b += c) ^ c ^ d) + in[5] + -378558;
        a = a << 4 | a >>> 28;
        d += ((a += b) ^ b ^ c) + in[8] + -2022574463;
        d = d << 11 | d >>> 21;
        c += ((d += a) ^ a ^ b) + in[11] + 1839030562;
        c = c << 16 | c >>> 16;
        b += ((c += d) ^ d ^ a) + in[14] + -35309556;
        b = b << 23 | b >>> 9;
        a += ((b += c) ^ c ^ d) + in[1] + -1530992060;
        a = a << 4 | a >>> 28;
        d += ((a += b) ^ b ^ c) + in[4] + 1272893353;
        d = d << 11 | d >>> 21;
        c += ((d += a) ^ a ^ b) + in[7] + -155497632;
        c = c << 16 | c >>> 16;
        b += ((c += d) ^ d ^ a) + in[10] + -1094730640;
        b = b << 23 | b >>> 9;
        a += ((b += c) ^ c ^ d) + in[13] + 681279174;
        a = a << 4 | a >>> 28;
        d += ((a += b) ^ b ^ c) + in[0] + -358537222;
        d = d << 11 | d >>> 21;
        c += ((d += a) ^ a ^ b) + in[3] + -722521979;
        c = c << 16 | c >>> 16;
        b += ((c += d) ^ d ^ a) + in[6] + 76029189;
        b = b << 23 | b >>> 9;
        a += ((b += c) ^ c ^ d) + in[9] + -640364487;
        a = a << 4 | a >>> 28;
        d += ((a += b) ^ b ^ c) + in[12] + -421815835;
        d = d << 11 | d >>> 21;
        c += ((d += a) ^ a ^ b) + in[15] + 530742520;
        c = c << 16 | c >>> 16;
        b += ((c += d) ^ d ^ a) + in[2] + -995338651;
        b = b << 23 | b >>> 9;
        a += (c ^ ((b += c) | ~d)) + in[0] + -198630844;
        a = a << 6 | a >>> 26;
        d += (b ^ ((a += b) | ~c)) + in[7] + 1126891415;
        d = d << 10 | d >>> 22;
        c += (a ^ ((d += a) | ~b)) + in[14] + -1416354905;
        c = c << 15 | c >>> 17;
        b += (d ^ ((c += d) | ~a)) + in[5] + -57434055;
        b = b << 21 | b >>> 11;
        a += (c ^ ((b += c) | ~d)) + in[12] + 1700485571;
        a = a << 6 | a >>> 26;
        d += (b ^ ((a += b) | ~c)) + in[3] + -1894986606;
        d = d << 10 | d >>> 22;
        c += (a ^ ((d += a) | ~b)) + in[10] + -1051523;
        c = c << 15 | c >>> 17;
        b += (d ^ ((c += d) | ~a)) + in[1] + -2054922799;
        b = b << 21 | b >>> 11;
        a += (c ^ ((b += c) | ~d)) + in[8] + 1873313359;
        a = a << 6 | a >>> 26;
        d += (b ^ ((a += b) | ~c)) + in[15] + -30611744;
        d = d << 10 | d >>> 22;
        c += (a ^ ((d += a) | ~b)) + in[6] + -1560198380;
        c = c << 15 | c >>> 17;
        b += (d ^ ((c += d) | ~a)) + in[13] + 1309151649;
        b = b << 21 | b >>> 11;
        a += (c ^ ((b += c) | ~d)) + in[4] + -145523070;
        a = a << 6 | a >>> 26;
        d += (b ^ ((a += b) | ~c)) + in[11] + -1120210379;
        d = d << 10 | d >>> 22;
        c += (a ^ ((d += a) | ~b)) + in[2] + 718787259;
        c = c << 15 | c >>> 17;
        b += (d ^ ((c += d) | ~a)) + in[9] + -343485551;
        b = b << 21 | b >>> 11;
        buf[0] = buf[0] + a;
        buf[1] = buf[1] + (b += c);
        buf[2] = buf[2] + c;
        buf[3] = buf[3] + d;
    }

    static class MD5Context {
        int[] buf = new int[4];
        int[] bits = new int[2];
        byte[] in = new byte[64];
    }
}

