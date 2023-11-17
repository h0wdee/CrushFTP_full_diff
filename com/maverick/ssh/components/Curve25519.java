/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

public class Curve25519 {
    public static final int KEY_SIZE = 32;
    public static final byte[] ZERO = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final byte[] PRIME = new byte[]{-19, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 127};
    public static final byte[] ORDER = new byte[]{-19, -45, -11, 92, 26, 99, 18, 88, -42, -100, -9, -94, -34, -7, -34, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16};
    private static final int P25 = 0x1FFFFFF;
    private static final int P26 = 0x3FFFFFF;
    private static final byte[] ORDER_TIMES_8 = new byte[]{104, -97, -82, -25, -46, 24, -109, -64, -78, -26, -68, 23, -11, -50, -9, -90, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -128};
    private static final long10 BASE_2Y = new long10(39999547L, 18689728L, 59995525L, 1648697L, 57546132L, 24010086L, 19059592L, 5425144L, 63499247L, 16420658L);
    private static final long10 BASE_R2Y = new long10(5744L, 8160848L, 4790893L, 13779497L, 35730846L, 12541209L, 49101323L, 30047407L, 40071253L, 6226132L);

    public static final void clamp(byte[] k) {
        k[31] = (byte)(k[31] & 0x7F);
        k[31] = (byte)(k[31] | 0x40);
        k[0] = (byte)(k[0] & 0xF8);
    }

    public static final void keygen(byte[] P, byte[] s, byte[] k) {
        Curve25519.clamp(k);
        Curve25519.core(P, s, k, null);
    }

    public static final void curve(byte[] Z, byte[] k, byte[] P) {
        Curve25519.core(Z, null, k, P);
    }

    public static final boolean sign(byte[] v, byte[] h, byte[] x, byte[] s) {
        byte[] h1 = new byte[32];
        byte[] x1 = new byte[32];
        byte[] tmp1 = new byte[64];
        byte[] tmp2 = new byte[64];
        Curve25519.cpy32(h1, h);
        Curve25519.cpy32(x1, x);
        byte[] tmp3 = new byte[32];
        Curve25519.divmod(tmp3, h1, 32, ORDER, 32);
        Curve25519.divmod(tmp3, x1, 32, ORDER, 32);
        Curve25519.mula_small(v, x1, 0, h1, 32, -1);
        Curve25519.mula_small(v, v, 0, ORDER, 32, 1);
        Curve25519.mula32(tmp1, v, s, 32, 1);
        Curve25519.divmod(tmp2, tmp1, 64, ORDER, 32);
        int w = 0;
        for (int i = 0; i < 32; ++i) {
            v[i] = tmp1[i];
            w |= v[i];
        }
        return w != 0;
    }

    public static final void verify(byte[] Y, byte[] v, byte[] h, byte[] P) {
        int k;
        int i;
        byte[] d = new byte[32];
        long10[] p = new long10[]{new long10(), new long10()};
        long10[] s = new long10[]{new long10(), new long10()};
        long10[] yx = new long10[]{new long10(), new long10(), new long10()};
        long10[] yz = new long10[]{new long10(), new long10(), new long10()};
        long10[] t1 = new long10[]{new long10(), new long10(), new long10()};
        long10[] t2 = new long10[]{new long10(), new long10(), new long10()};
        int vi = 0;
        int hi = 0;
        int di = 0;
        int nvh = 0;
        Curve25519.set(p[0], 9);
        Curve25519.unpack(p[1], P);
        Curve25519.x_to_y2(t1[0], t2[0], p[1]);
        Curve25519.sqrt(t1[0], t2[0]);
        int j = Curve25519.is_negative(t1[0]);
        t2[0]._0 += 39420360L;
        Curve25519.mul(t2[1], BASE_2Y, t1[0]);
        Curve25519.sub(t1[j], t2[0], t2[1]);
        Curve25519.add(t1[1 - j], t2[0], t2[1]);
        Curve25519.cpy(t2[0], p[1]);
        t2[0]._0 -= 9L;
        Curve25519.sqr(t2[1], t2[0]);
        Curve25519.recip(t2[0], t2[1], 0);
        Curve25519.mul(s[0], t1[0], t2[0]);
        Curve25519.sub(s[0], s[0], p[1]);
        s[0]._0 -= 486671L;
        Curve25519.mul(s[1], t1[1], t2[0]);
        Curve25519.sub(s[1], s[1], p[1]);
        s[1]._0 -= 486671L;
        Curve25519.mul_small(s[0], s[0], 1L);
        Curve25519.mul_small(s[1], s[1], 1L);
        for (i = 0; i < 32; ++i) {
            vi = vi >> 8 ^ v[i] & 0xFF ^ (v[i] & 0xFF) << 1;
            hi = hi >> 8 ^ h[i] & 0xFF ^ (h[i] & 0xFF) << 1;
            nvh = ~(vi ^ hi);
            di = nvh & (di & 0x80) >> 7 ^ vi;
            di ^= nvh & (di & 1) << 1;
            di ^= nvh & (di & 2) << 1;
            di ^= nvh & (di & 4) << 1;
            di ^= nvh & (di & 8) << 1;
            di ^= nvh & (di & 0x10) << 1;
            di ^= nvh & (di & 0x20) << 1;
            di ^= nvh & (di & 0x40) << 1;
            d[i] = (byte)di;
        }
        di = (nvh & (di & 0x80) << 1 ^ vi) >> 8;
        Curve25519.set(yx[0], 1);
        Curve25519.cpy(yx[1], p[di]);
        Curve25519.cpy(yx[2], s[0]);
        Curve25519.set(yz[0], 0);
        Curve25519.set(yz[1], 1);
        Curve25519.set(yz[2], 1);
        vi = 0;
        hi = 0;
        i = 32;
        while (i-- != 0) {
            vi = vi << 8 | v[i] & 0xFF;
            hi = hi << 8 | h[i] & 0xFF;
            di = di << 8 | d[i] & 0xFF;
            j = 8;
            while (j-- != 0) {
                Curve25519.mont_prep(t1[0], t2[0], yx[0], yz[0]);
                Curve25519.mont_prep(t1[1], t2[1], yx[1], yz[1]);
                Curve25519.mont_prep(t1[2], t2[2], yx[2], yz[2]);
                k = ((vi ^ vi >> 1) >> j & 1) + ((hi ^ hi >> 1) >> j & 1);
                Curve25519.mont_dbl(yx[2], yz[2], t1[k], t2[k], yx[0], yz[0]);
                k = di >> j & 2 ^ (di >> j & 1) << 1;
                Curve25519.mont_add(t1[1], t2[1], t1[k], t2[k], yx[1], yz[1], p[di >> j & 1]);
                Curve25519.mont_add(t1[2], t2[2], t1[0], t2[0], yx[2], yz[2], s[((vi ^ hi) >> j & 2) >> 1]);
            }
        }
        k = (vi & 1) + (hi & 1);
        Curve25519.recip(t1[0], yz[k], 0);
        Curve25519.mul(t1[1], yx[k], t1[0]);
        Curve25519.pack(t1[1], Y);
    }

    private static final void cpy32(byte[] d, byte[] s) {
        for (int i = 0; i < 32; ++i) {
            d[i] = s[i];
        }
    }

    private static final int mula_small(byte[] p, byte[] q, int m, byte[] x, int n, int z) {
        int v = 0;
        for (int i = 0; i < n; ++i) {
            p[i + m] = (byte)(v += (q[i + m] & 0xFF) + z * (x[i] & 0xFF));
            v >>= 8;
        }
        return v;
    }

    private static final int mula32(byte[] p, byte[] x, byte[] y, int t, int z) {
        int i;
        int n = 31;
        int w = 0;
        for (i = 0; i < t; ++i) {
            int zy = z * (y[i] & 0xFF);
            p[i + 31] = (byte)(w += Curve25519.mula_small(p, p, i, x, 31, zy) + (p[i + 31] & 0xFF) + zy * (x[31] & 0xFF));
            w >>= 8;
        }
        p[i + 31] = (byte)(w + (p[i + 31] & 0xFF));
        return w >> 8;
    }

    private static final void divmod(byte[] q, byte[] r, int n, byte[] d, int t) {
        int rn = 0;
        int dt = (d[t - 1] & 0xFF) << 8;
        if (t > 1) {
            dt |= d[t - 2] & 0xFF;
        }
        while (n-- >= t) {
            int z = rn << 16 | (r[n] & 0xFF) << 8;
            if (n > 0) {
                z |= r[n - 1] & 0xFF;
            }
            q[n - t + 1] = (byte)(z + (rn += Curve25519.mula_small(r, r, n - t + 1, d, t, -(z /= dt))) & 0xFF);
            Curve25519.mula_small(r, r, n - t + 1, d, t, -rn);
            rn = r[n] & 0xFF;
            r[n] = 0;
        }
        r[t - 1] = (byte)rn;
    }

    private static final int numsize(byte[] x, int n) {
        while (n-- != 0 && x[n] == 0) {
        }
        return n + 1;
    }

    private static final byte[] egcd32(byte[] x, byte[] y, byte[] a, byte[] b) {
        int bn = 32;
        for (int i = 0; i < 32; ++i) {
            y[i] = 0;
            x[i] = 0;
        }
        x[0] = 1;
        int an = Curve25519.numsize(a, 32);
        if (an == 0) {
            return y;
        }
        byte[] temp = new byte[32];
        while (true) {
            int qn = bn - an + 1;
            Curve25519.divmod(temp, b, bn, a, an);
            bn = Curve25519.numsize(b, bn);
            if (bn == 0) {
                return x;
            }
            Curve25519.mula32(y, x, temp, qn, -1);
            qn = an - bn + 1;
            Curve25519.divmod(temp, a, an, b, bn);
            an = Curve25519.numsize(a, an);
            if (an == 0) {
                return y;
            }
            Curve25519.mula32(x, y, temp, qn, -1);
        }
    }

    private static final void unpack(long10 x, byte[] m) {
        x._0 = m[0] & 0xFF | (m[1] & 0xFF) << 8 | (m[2] & 0xFF) << 16 | (m[3] & 0xFF & 3) << 24;
        x._1 = (m[3] & 0xFF & 0xFFFFFFFC) >> 2 | (m[4] & 0xFF) << 6 | (m[5] & 0xFF) << 14 | (m[6] & 0xFF & 7) << 22;
        x._2 = (m[6] & 0xFF & 0xFFFFFFF8) >> 3 | (m[7] & 0xFF) << 5 | (m[8] & 0xFF) << 13 | (m[9] & 0xFF & 0x1F) << 21;
        x._3 = (m[9] & 0xFF & 0xFFFFFFE0) >> 5 | (m[10] & 0xFF) << 3 | (m[11] & 0xFF) << 11 | (m[12] & 0xFF & 0x3F) << 19;
        x._4 = (m[12] & 0xFF & 0xFFFFFFC0) >> 6 | (m[13] & 0xFF) << 2 | (m[14] & 0xFF) << 10 | (m[15] & 0xFF) << 18;
        x._5 = m[16] & 0xFF | (m[17] & 0xFF) << 8 | (m[18] & 0xFF) << 16 | (m[19] & 0xFF & 1) << 24;
        x._6 = (m[19] & 0xFF & 0xFFFFFFFE) >> 1 | (m[20] & 0xFF) << 7 | (m[21] & 0xFF) << 15 | (m[22] & 0xFF & 7) << 23;
        x._7 = (m[22] & 0xFF & 0xFFFFFFF8) >> 3 | (m[23] & 0xFF) << 5 | (m[24] & 0xFF) << 13 | (m[25] & 0xFF & 0xF) << 21;
        x._8 = (m[25] & 0xFF & 0xFFFFFFF0) >> 4 | (m[26] & 0xFF) << 4 | (m[27] & 0xFF) << 12 | (m[28] & 0xFF & 0x3F) << 20;
        x._9 = (m[28] & 0xFF & 0xFFFFFFC0) >> 6 | (m[29] & 0xFF) << 2 | (m[30] & 0xFF) << 10 | (m[31] & 0xFF) << 18;
    }

    private static final boolean is_overflow(long10 x) {
        return x._0 > 67108844L && (x._1 & x._3 & x._5 & x._7 & x._9) == 0x1FFFFFFL && (x._2 & x._4 & x._6 & x._8) == 0x3FFFFFFL || x._9 > 0x1FFFFFFL;
    }

    private static final void pack(long10 x, byte[] m) {
        int ld = 0;
        int ud = 0;
        ld = (Curve25519.is_overflow(x) ? 1 : 0) - (x._9 < 0L ? 1 : 0);
        ud = ld * -33554432;
        long t = (long)(ld *= 19) + x._0 + (x._1 << 26);
        m[0] = (byte)t;
        m[1] = (byte)(t >> 8);
        m[2] = (byte)(t >> 16);
        m[3] = (byte)(t >> 24);
        t = (t >> 32) + (x._2 << 19);
        m[4] = (byte)t;
        m[5] = (byte)(t >> 8);
        m[6] = (byte)(t >> 16);
        m[7] = (byte)(t >> 24);
        t = (t >> 32) + (x._3 << 13);
        m[8] = (byte)t;
        m[9] = (byte)(t >> 8);
        m[10] = (byte)(t >> 16);
        m[11] = (byte)(t >> 24);
        t = (t >> 32) + (x._4 << 6);
        m[12] = (byte)t;
        m[13] = (byte)(t >> 8);
        m[14] = (byte)(t >> 16);
        m[15] = (byte)(t >> 24);
        t = (t >> 32) + x._5 + (x._6 << 25);
        m[16] = (byte)t;
        m[17] = (byte)(t >> 8);
        m[18] = (byte)(t >> 16);
        m[19] = (byte)(t >> 24);
        t = (t >> 32) + (x._7 << 19);
        m[20] = (byte)t;
        m[21] = (byte)(t >> 8);
        m[22] = (byte)(t >> 16);
        m[23] = (byte)(t >> 24);
        t = (t >> 32) + (x._8 << 12);
        m[24] = (byte)t;
        m[25] = (byte)(t >> 8);
        m[26] = (byte)(t >> 16);
        m[27] = (byte)(t >> 24);
        t = (t >> 32) + (x._9 + (long)ud << 6);
        m[28] = (byte)t;
        m[29] = (byte)(t >> 8);
        m[30] = (byte)(t >> 16);
        m[31] = (byte)(t >> 24);
    }

    private static final void cpy(long10 out, long10 in) {
        out._0 = in._0;
        out._1 = in._1;
        out._2 = in._2;
        out._3 = in._3;
        out._4 = in._4;
        out._5 = in._5;
        out._6 = in._6;
        out._7 = in._7;
        out._8 = in._8;
        out._9 = in._9;
    }

    private static final void set(long10 out, int in) {
        out._0 = in;
        out._1 = 0L;
        out._2 = 0L;
        out._3 = 0L;
        out._4 = 0L;
        out._5 = 0L;
        out._6 = 0L;
        out._7 = 0L;
        out._8 = 0L;
        out._9 = 0L;
    }

    private static final void add(long10 xy, long10 x, long10 y) {
        xy._0 = x._0 + y._0;
        xy._1 = x._1 + y._1;
        xy._2 = x._2 + y._2;
        xy._3 = x._3 + y._3;
        xy._4 = x._4 + y._4;
        xy._5 = x._5 + y._5;
        xy._6 = x._6 + y._6;
        xy._7 = x._7 + y._7;
        xy._8 = x._8 + y._8;
        xy._9 = x._9 + y._9;
    }

    private static final void sub(long10 xy, long10 x, long10 y) {
        xy._0 = x._0 - y._0;
        xy._1 = x._1 - y._1;
        xy._2 = x._2 - y._2;
        xy._3 = x._3 - y._3;
        xy._4 = x._4 - y._4;
        xy._5 = x._5 - y._5;
        xy._6 = x._6 - y._6;
        xy._7 = x._7 - y._7;
        xy._8 = x._8 - y._8;
        xy._9 = x._9 - y._9;
    }

    private static final long10 mul_small(long10 xy, long10 x, long y) {
        long t = x._8 * y;
        xy._8 = t & 0x3FFFFFFL;
        t = (t >> 26) + x._9 * y;
        xy._9 = t & 0x1FFFFFFL;
        t = 19L * (t >> 25) + x._0 * y;
        xy._0 = t & 0x3FFFFFFL;
        t = (t >> 26) + x._1 * y;
        xy._1 = t & 0x1FFFFFFL;
        t = (t >> 25) + x._2 * y;
        xy._2 = t & 0x3FFFFFFL;
        t = (t >> 26) + x._3 * y;
        xy._3 = t & 0x1FFFFFFL;
        t = (t >> 25) + x._4 * y;
        xy._4 = t & 0x3FFFFFFL;
        t = (t >> 26) + x._5 * y;
        xy._5 = t & 0x1FFFFFFL;
        t = (t >> 25) + x._6 * y;
        xy._6 = t & 0x3FFFFFFL;
        t = (t >> 26) + x._7 * y;
        xy._7 = t & 0x1FFFFFFL;
        t = (t >> 25) + xy._8;
        xy._8 = t & 0x3FFFFFFL;
        xy._9 += t >> 26;
        return xy;
    }

    private static final long10 mul(long10 xy, long10 x, long10 y) {
        long x_0 = x._0;
        long x_1 = x._1;
        long x_2 = x._2;
        long x_3 = x._3;
        long x_4 = x._4;
        long x_5 = x._5;
        long x_6 = x._6;
        long x_7 = x._7;
        long x_8 = x._8;
        long x_9 = x._9;
        long y_0 = y._0;
        long y_1 = y._1;
        long y_2 = y._2;
        long y_3 = y._3;
        long y_4 = y._4;
        long y_5 = y._5;
        long y_6 = y._6;
        long y_7 = y._7;
        long y_8 = y._8;
        long y_9 = y._9;
        long t = x_0 * y_8 + x_2 * y_6 + x_4 * y_4 + x_6 * y_2 + x_8 * y_0 + 2L * (x_1 * y_7 + x_3 * y_5 + x_5 * y_3 + x_7 * y_1) + 38L * (x_9 * y_9);
        xy._8 = t & 0x3FFFFFFL;
        t = (t >> 26) + x_0 * y_9 + x_1 * y_8 + x_2 * y_7 + x_3 * y_6 + x_4 * y_5 + x_5 * y_4 + x_6 * y_3 + x_7 * y_2 + x_8 * y_1 + x_9 * y_0;
        xy._9 = t & 0x1FFFFFFL;
        t = x_0 * y_0 + 19L * ((t >> 25) + x_2 * y_8 + x_4 * y_6 + x_6 * y_4 + x_8 * y_2) + 38L * (x_1 * y_9 + x_3 * y_7 + x_5 * y_5 + x_7 * y_3 + x_9 * y_1);
        xy._0 = t & 0x3FFFFFFL;
        t = (t >> 26) + x_0 * y_1 + x_1 * y_0 + 19L * (x_2 * y_9 + x_3 * y_8 + x_4 * y_7 + x_5 * y_6 + x_6 * y_5 + x_7 * y_4 + x_8 * y_3 + x_9 * y_2);
        xy._1 = t & 0x1FFFFFFL;
        t = (t >> 25) + x_0 * y_2 + x_2 * y_0 + 19L * (x_4 * y_8 + x_6 * y_6 + x_8 * y_4) + 2L * (x_1 * y_1) + 38L * (x_3 * y_9 + x_5 * y_7 + x_7 * y_5 + x_9 * y_3);
        xy._2 = t & 0x3FFFFFFL;
        t = (t >> 26) + x_0 * y_3 + x_1 * y_2 + x_2 * y_1 + x_3 * y_0 + 19L * (x_4 * y_9 + x_5 * y_8 + x_6 * y_7 + x_7 * y_6 + x_8 * y_5 + x_9 * y_4);
        xy._3 = t & 0x1FFFFFFL;
        t = (t >> 25) + x_0 * y_4 + x_2 * y_2 + x_4 * y_0 + 19L * (x_6 * y_8 + x_8 * y_6) + 2L * (x_1 * y_3 + x_3 * y_1) + 38L * (x_5 * y_9 + x_7 * y_7 + x_9 * y_5);
        xy._4 = t & 0x3FFFFFFL;
        t = (t >> 26) + x_0 * y_5 + x_1 * y_4 + x_2 * y_3 + x_3 * y_2 + x_4 * y_1 + x_5 * y_0 + 19L * (x_6 * y_9 + x_7 * y_8 + x_8 * y_7 + x_9 * y_6);
        xy._5 = t & 0x1FFFFFFL;
        t = (t >> 25) + x_0 * y_6 + x_2 * y_4 + x_4 * y_2 + x_6 * y_0 + 19L * (x_8 * y_8) + 2L * (x_1 * y_5 + x_3 * y_3 + x_5 * y_1) + 38L * (x_7 * y_9 + x_9 * y_7);
        xy._6 = t & 0x3FFFFFFL;
        t = (t >> 26) + x_0 * y_7 + x_1 * y_6 + x_2 * y_5 + x_3 * y_4 + x_4 * y_3 + x_5 * y_2 + x_6 * y_1 + x_7 * y_0 + 19L * (x_8 * y_9 + x_9 * y_8);
        xy._7 = t & 0x1FFFFFFL;
        t = (t >> 25) + xy._8;
        xy._8 = t & 0x3FFFFFFL;
        xy._9 += t >> 26;
        return xy;
    }

    private static final long10 sqr(long10 x2, long10 x) {
        long x_0 = x._0;
        long x_1 = x._1;
        long x_2 = x._2;
        long x_3 = x._3;
        long x_4 = x._4;
        long x_5 = x._5;
        long x_6 = x._6;
        long x_7 = x._7;
        long x_8 = x._8;
        long x_9 = x._9;
        long t = x_4 * x_4 + 2L * (x_0 * x_8 + x_2 * x_6) + 38L * (x_9 * x_9) + 4L * (x_1 * x_7 + x_3 * x_5);
        x2._8 = t & 0x3FFFFFFL;
        t = (t >> 26) + 2L * (x_0 * x_9 + x_1 * x_8 + x_2 * x_7 + x_3 * x_6 + x_4 * x_5);
        x2._9 = t & 0x1FFFFFFL;
        t = 19L * (t >> 25) + x_0 * x_0 + 38L * (x_2 * x_8 + x_4 * x_6 + x_5 * x_5) + 76L * (x_1 * x_9 + x_3 * x_7);
        x2._0 = t & 0x3FFFFFFL;
        t = (t >> 26) + 2L * (x_0 * x_1) + 38L * (x_2 * x_9 + x_3 * x_8 + x_4 * x_7 + x_5 * x_6);
        x2._1 = t & 0x1FFFFFFL;
        t = (t >> 25) + 19L * (x_6 * x_6) + 2L * (x_0 * x_2 + x_1 * x_1) + 38L * (x_4 * x_8) + 76L * (x_3 * x_9 + x_5 * x_7);
        x2._2 = t & 0x3FFFFFFL;
        t = (t >> 26) + 2L * (x_0 * x_3 + x_1 * x_2) + 38L * (x_4 * x_9 + x_5 * x_8 + x_6 * x_7);
        x2._3 = t & 0x1FFFFFFL;
        t = (t >> 25) + x_2 * x_2 + 2L * (x_0 * x_4) + 38L * (x_6 * x_8 + x_7 * x_7) + 4L * (x_1 * x_3) + 76L * (x_5 * x_9);
        x2._4 = t & 0x3FFFFFFL;
        t = (t >> 26) + 2L * (x_0 * x_5 + x_1 * x_4 + x_2 * x_3) + 38L * (x_6 * x_9 + x_7 * x_8);
        x2._5 = t & 0x1FFFFFFL;
        t = (t >> 25) + 19L * (x_8 * x_8) + 2L * (x_0 * x_6 + x_2 * x_4 + x_3 * x_3) + 4L * (x_1 * x_5) + 76L * (x_7 * x_9);
        x2._6 = t & 0x3FFFFFFL;
        t = (t >> 26) + 2L * (x_0 * x_7 + x_1 * x_6 + x_2 * x_5 + x_3 * x_4) + 38L * (x_8 * x_9);
        x2._7 = t & 0x1FFFFFFL;
        t = (t >> 25) + x2._8;
        x2._8 = t & 0x3FFFFFFL;
        x2._9 += t >> 26;
        return x2;
    }

    private static final void recip(long10 y, long10 x, int sqrtassist) {
        int i;
        long10 t0 = new long10();
        long10 t1 = new long10();
        long10 t2 = new long10();
        long10 t3 = new long10();
        long10 t4 = new long10();
        Curve25519.sqr(t1, x);
        Curve25519.sqr(t2, t1);
        Curve25519.sqr(t0, t2);
        Curve25519.mul(t2, t0, x);
        Curve25519.mul(t0, t2, t1);
        Curve25519.sqr(t1, t0);
        Curve25519.mul(t3, t1, t2);
        Curve25519.sqr(t1, t3);
        Curve25519.sqr(t2, t1);
        Curve25519.sqr(t1, t2);
        Curve25519.sqr(t2, t1);
        Curve25519.sqr(t1, t2);
        Curve25519.mul(t2, t1, t3);
        Curve25519.sqr(t1, t2);
        Curve25519.sqr(t3, t1);
        for (i = 1; i < 5; ++i) {
            Curve25519.sqr(t1, t3);
            Curve25519.sqr(t3, t1);
        }
        Curve25519.mul(t1, t3, t2);
        Curve25519.sqr(t3, t1);
        Curve25519.sqr(t4, t3);
        for (i = 1; i < 10; ++i) {
            Curve25519.sqr(t3, t4);
            Curve25519.sqr(t4, t3);
        }
        Curve25519.mul(t3, t4, t1);
        for (i = 0; i < 5; ++i) {
            Curve25519.sqr(t1, t3);
            Curve25519.sqr(t3, t1);
        }
        Curve25519.mul(t1, t3, t2);
        Curve25519.sqr(t2, t1);
        Curve25519.sqr(t3, t2);
        for (i = 1; i < 25; ++i) {
            Curve25519.sqr(t2, t3);
            Curve25519.sqr(t3, t2);
        }
        Curve25519.mul(t2, t3, t1);
        Curve25519.sqr(t3, t2);
        Curve25519.sqr(t4, t3);
        for (i = 1; i < 50; ++i) {
            Curve25519.sqr(t3, t4);
            Curve25519.sqr(t4, t3);
        }
        Curve25519.mul(t3, t4, t2);
        for (i = 0; i < 25; ++i) {
            Curve25519.sqr(t4, t3);
            Curve25519.sqr(t3, t4);
        }
        Curve25519.mul(t2, t3, t1);
        Curve25519.sqr(t1, t2);
        Curve25519.sqr(t2, t1);
        if (sqrtassist != 0) {
            Curve25519.mul(y, x, t2);
        } else {
            Curve25519.sqr(t1, t2);
            Curve25519.sqr(t2, t1);
            Curve25519.sqr(t1, t2);
            Curve25519.mul(y, t1, t0);
        }
    }

    private static final int is_negative(long10 x) {
        return (int)((long)(Curve25519.is_overflow(x) || x._9 < 0L ? 1 : 0) ^ x._0 & 1L);
    }

    private static final void sqrt(long10 x, long10 u) {
        long10 v = new long10();
        long10 t1 = new long10();
        long10 t2 = new long10();
        Curve25519.add(t1, u, u);
        Curve25519.recip(v, t1, 1);
        Curve25519.sqr(x, v);
        Curve25519.mul(t2, t1, x);
        --t2._0;
        Curve25519.mul(t1, v, t2);
        Curve25519.mul(x, u, t1);
    }

    private static final void mont_prep(long10 t1, long10 t2, long10 ax, long10 az) {
        Curve25519.add(t1, ax, az);
        Curve25519.sub(t2, ax, az);
    }

    private static final void mont_add(long10 t1, long10 t2, long10 t3, long10 t4, long10 ax, long10 az, long10 dx) {
        Curve25519.mul(ax, t2, t3);
        Curve25519.mul(az, t1, t4);
        Curve25519.add(t1, ax, az);
        Curve25519.sub(t2, ax, az);
        Curve25519.sqr(ax, t1);
        Curve25519.sqr(t1, t2);
        Curve25519.mul(az, t1, dx);
    }

    private static final void mont_dbl(long10 t1, long10 t2, long10 t3, long10 t4, long10 bx, long10 bz) {
        Curve25519.sqr(t1, t3);
        Curve25519.sqr(t2, t4);
        Curve25519.mul(bx, t1, t2);
        Curve25519.sub(t2, t1, t2);
        Curve25519.mul_small(bz, t2, 121665L);
        Curve25519.add(t1, t1, bz);
        Curve25519.mul(bz, t1, t2);
    }

    private static final void x_to_y2(long10 t, long10 y2, long10 x) {
        Curve25519.sqr(t, x);
        Curve25519.mul_small(y2, x, 486662L);
        Curve25519.add(t, t, y2);
        ++t._0;
        Curve25519.mul(y2, t, x);
    }

    private static final void core(byte[] Px, byte[] s, byte[] k, byte[] Gx) {
        long10 dx = new long10();
        long10 t1 = new long10();
        long10 t2 = new long10();
        long10 t3 = new long10();
        long10 t4 = new long10();
        long10[] x = new long10[]{new long10(), new long10()};
        long10[] z = new long10[]{new long10(), new long10()};
        if (Gx != null) {
            Curve25519.unpack(dx, Gx);
        } else {
            Curve25519.set(dx, 9);
        }
        Curve25519.set(x[0], 1);
        Curve25519.set(z[0], 0);
        Curve25519.cpy(x[1], dx);
        Curve25519.set(z[1], 1);
        int i = 32;
        while (i-- != 0) {
            if (i == 0) {
                i = 0;
            }
            int j = 8;
            while (j-- != 0) {
                int bit1 = (k[i] & 0xFF) >> j & 1;
                int bit0 = ~(k[i] & 0xFF) >> j & 1;
                long10 ax = x[bit0];
                long10 az = z[bit0];
                long10 bx = x[bit1];
                long10 bz = z[bit1];
                Curve25519.mont_prep(t1, t2, ax, az);
                Curve25519.mont_prep(t3, t4, bx, bz);
                Curve25519.mont_add(t1, t2, t3, t4, ax, az, dx);
                Curve25519.mont_dbl(t1, t2, t3, t4, bx, bz);
            }
        }
        Curve25519.recip(t1, z[0], 0);
        Curve25519.mul(dx, x[0], t1);
        Curve25519.pack(dx, Px);
        if (s != null) {
            Curve25519.x_to_y2(t2, t1, dx);
            Curve25519.recip(t3, z[1], 0);
            Curve25519.mul(t2, x[1], t3);
            Curve25519.add(t2, t2, dx);
            t2._0 += 486671L;
            dx._0 -= 9L;
            Curve25519.sqr(t3, dx);
            Curve25519.mul(dx, t2, t3);
            Curve25519.sub(dx, dx, t1);
            dx._0 -= 39420360L;
            Curve25519.mul(t1, dx, BASE_R2Y);
            if (Curve25519.is_negative(t1) != 0) {
                Curve25519.cpy32(s, k);
            } else {
                Curve25519.mula_small(s, ORDER_TIMES_8, 0, k, 32, -1);
            }
            byte[] temp1 = new byte[32];
            byte[] temp2 = new byte[64];
            byte[] temp3 = new byte[64];
            Curve25519.cpy32(temp1, ORDER);
            Curve25519.cpy32(s, Curve25519.egcd32(temp2, temp3, s, temp1));
            if ((s[31] & 0x80) != 0) {
                Curve25519.mula_small(s, s, 0, ORDER, 32, 1);
            }
        }
    }

    private static final class long10 {
        public long _0;
        public long _1;
        public long _2;
        public long _3;
        public long _4;
        public long _5;
        public long _6;
        public long _7;
        public long _8;
        public long _9;

        public long10() {
        }

        public long10(long _0, long _1, long _2, long _3, long _4, long _5, long _6, long _7, long _8, long _9) {
            this._0 = _0;
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
            this._7 = _7;
            this._8 = _8;
            this._9 = _9;
        }
    }
}

