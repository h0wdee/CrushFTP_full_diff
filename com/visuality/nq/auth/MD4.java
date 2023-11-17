/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import java.util.Arrays;

public class MD4 {
    public static void cmMD4(byte[] out, byte[] in, int n) {
        MD4.MD4Internal(in, out, n);
    }

    private static void MD4Internal(byte[] in, byte[] out, int n) {
        int i;
        int i2;
        MD4Context ctx = new MD4Context();
        byte[] buffer = new byte[128];
        int[] M = new int[16];
        int b = n * 8;
        byte[] tmp = new byte[4];
        ctx.a = 1732584193;
        ctx.b = -271733879;
        ctx.c = -1732584194;
        ctx.d = 271733878;
        while (n > 64) {
            byte[] tmpBuff = new byte[in.length - 64];
            MD4.MD4_Transform(ctx, in);
            System.arraycopy(in, 64, tmpBuff, 0, tmpBuff.length);
            in = tmpBuff;
            n -= 64;
        }
        Arrays.fill(buffer, (byte)0);
        for (i2 = 0; i2 < in.length; ++i2) {
            buffer[i2] = in[i2];
        }
        buffer[n] = -128;
        MD4.copy4(tmp, b);
        if (n <= 55) {
            for (i2 = 0; i2 < 4; ++i2) {
                buffer[i2 + 56] = tmp[i2];
            }
            MD4.MD4_Transform(ctx, buffer);
        } else {
            for (i2 = 0; i2 < 4; ++i2) {
                buffer[i2 + 120] = tmp[i2];
            }
            MD4.MD4_Transform(ctx, buffer);
            byte[] tmpBuff = new byte[64];
            System.arraycopy(buffer, 64, tmpBuff, 0, tmpBuff.length);
            MD4.MD4_Transform(ctx, tmpBuff);
        }
        Arrays.fill(buffer, (byte)0);
        MD4.copy64(M, buffer);
        MD4.copy4(out, ctx.a);
        MD4.copy4(tmp, ctx.b);
        for (i = 0; i < 4; ++i) {
            out[i + 4] = tmp[i];
        }
        MD4.copy4(tmp, ctx.c);
        for (i = 0; i < 4; ++i) {
            out[i + 8] = tmp[i];
        }
        MD4.copy4(tmp, ctx.d);
        for (i = 0; i < 4; ++i) {
            out[i + 12] = tmp[i];
        }
        ctx.d = 0;
        ctx.c = 0;
        ctx.b = 0;
        ctx.a = 0;
    }

    private static void MD4_Transform(MD4Context ctx, byte[] buffer) {
        int[] in = new int[16];
        MD4.copy64(in, buffer);
        int A = ctx.a;
        int B = ctx.b;
        int C = ctx.c;
        int D = ctx.d;
        A = MD4.lrotate(A + (B & C | ~B & D) + in[0], 3);
        D = MD4.lrotate(D + (A & B | ~A & C) + in[1], 7);
        C = MD4.lrotate(C + (D & A | ~D & B) + in[2], 11);
        B = MD4.lrotate(B + (C & D | ~C & A) + in[3], 19);
        A = MD4.lrotate(A + (B & C | ~B & D) + in[4], 3);
        D = MD4.lrotate(D + (A & B | ~A & C) + in[5], 7);
        C = MD4.lrotate(C + (D & A | ~D & B) + in[6], 11);
        B = MD4.lrotate(B + (C & D | ~C & A) + in[7], 19);
        A = MD4.lrotate(A + (B & C | ~B & D) + in[8], 3);
        D = MD4.lrotate(D + (A & B | ~A & C) + in[9], 7);
        C = MD4.lrotate(C + (D & A | ~D & B) + in[10], 11);
        B = MD4.lrotate(B + (C & D | ~C & A) + in[11], 19);
        A = MD4.lrotate(A + (B & C | ~B & D) + in[12], 3);
        D = MD4.lrotate(D + (A & B | ~A & C) + in[13], 7);
        C = MD4.lrotate(C + (D & A | ~D & B) + in[14], 11);
        B = MD4.lrotate(B + (C & D | ~C & A) + in[15], 19);
        A = MD4.lrotate(A + (B & C | B & D | C & D) + in[0] + 1518500249, 3);
        D = MD4.lrotate(D + (A & B | A & C | B & C) + in[4] + 1518500249, 5);
        C = MD4.lrotate(C + (D & A | D & B | A & B) + in[8] + 1518500249, 9);
        B = MD4.lrotate(B + (C & D | C & A | D & A) + in[12] + 1518500249, 13);
        A = MD4.lrotate(A + (B & C | B & D | C & D) + in[1] + 1518500249, 3);
        D = MD4.lrotate(D + (A & B | A & C | B & C) + in[5] + 1518500249, 5);
        C = MD4.lrotate(C + (D & A | D & B | A & B) + in[9] + 1518500249, 9);
        B = MD4.lrotate(B + (C & D | C & A | D & A) + in[13] + 1518500249, 13);
        A = MD4.lrotate(A + (B & C | B & D | C & D) + in[2] + 1518500249, 3);
        D = MD4.lrotate(D + (A & B | A & C | B & C) + in[6] + 1518500249, 5);
        C = MD4.lrotate(C + (D & A | D & B | A & B) + in[10] + 1518500249, 9);
        B = MD4.lrotate(B + (C & D | C & A | D & A) + in[14] + 1518500249, 13);
        A = MD4.lrotate(A + (B & C | B & D | C & D) + in[3] + 1518500249, 3);
        D = MD4.lrotate(D + (A & B | A & C | B & C) + in[7] + 1518500249, 5);
        C = MD4.lrotate(C + (D & A | D & B | A & B) + in[11] + 1518500249, 9);
        B = MD4.lrotate(B + (C & D | C & A | D & A) + in[15] + 1518500249, 13);
        A = MD4.lrotate(A + (B ^ C ^ D) + in[0] + 1859775393, 3);
        D = MD4.lrotate(D + (A ^ B ^ C) + in[8] + 1859775393, 9);
        C = MD4.lrotate(C + (D ^ A ^ B) + in[4] + 1859775393, 11);
        B = MD4.lrotate(B + (C ^ D ^ A) + in[12] + 1859775393, 15);
        A = MD4.lrotate(A + (B ^ C ^ D) + in[2] + 1859775393, 3);
        D = MD4.lrotate(D + (A ^ B ^ C) + in[10] + 1859775393, 9);
        C = MD4.lrotate(C + (D ^ A ^ B) + in[6] + 1859775393, 11);
        B = MD4.lrotate(B + (C ^ D ^ A) + in[14] + 1859775393, 15);
        A = MD4.lrotate(A + (B ^ C ^ D) + in[1] + 1859775393, 3);
        D = MD4.lrotate(D + (A ^ B ^ C) + in[9] + 1859775393, 9);
        C = MD4.lrotate(C + (D ^ A ^ B) + in[5] + 1859775393, 11);
        B = MD4.lrotate(B + (C ^ D ^ A) + in[13] + 1859775393, 15);
        A = MD4.lrotate(A + (B ^ C ^ D) + in[3] + 1859775393, 3);
        D = MD4.lrotate(D + (A ^ B ^ C) + in[11] + 1859775393, 9);
        C = MD4.lrotate(C + (D ^ A ^ B) + in[7] + 1859775393, 11);
        B = MD4.lrotate(B + (C ^ D ^ A) + in[15] + 1859775393, 15);
        ctx.a += A;
        ctx.b += B;
        ctx.c += C;
        ctx.d += D;
    }

    private static void copy4(byte[] out, int x) {
        out[0] = (byte)(x & 0xFF);
        out[1] = (byte)(x >>> 8 & 0xFF);
        out[2] = (byte)(x >>> 16 & 0xFF);
        out[3] = (byte)(x >>> 24 & 0xFF);
    }

    private static void copy64(int[] M, byte[] in) {
        for (int i = 0; i < 16; ++i) {
            M[i] = (0xFF & in[i * 4 + 3]) << 24 | (0xFF & in[i * 4 + 2]) << 16 | (0xFF & in[i * 4 + 1]) << 8 | (0xFF & in[i * 4 + 0]) << 0;
        }
    }

    private static int lrotate(int x, int s) {
        return x << s & 0xFFFFFFFF | x >>> 32 - s;
    }

    static class MD4Context {
        int a;
        int b;
        int c;
        int d;

        MD4Context() {
        }
    }
}

