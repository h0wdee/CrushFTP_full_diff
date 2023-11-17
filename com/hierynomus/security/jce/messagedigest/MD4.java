/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security.jce.messagedigest;

import java.security.DigestException;
import java.security.MessageDigest;

public class MD4
extends MessageDigest {
    public static final int BYTE_DIGEST_LENGTH = 16;
    public static final int BYTE_BLOCK_LENGTH = 64;
    private static final int A = 1732584193;
    private static final int B = -271733879;
    private static final int C = -1732584194;
    private static final int D = 271733878;
    private int a = 1732584193;
    private int b = -271733879;
    private int c = -1732584194;
    private int d = 271733878;
    private long msgLength;
    private final byte[] buffer = new byte[64];

    public MD4() {
        super("MD4");
    }

    @Override
    protected int engineGetDigestLength() {
        return 16;
    }

    @Override
    protected void engineUpdate(byte b) {
        int pos = (int)(this.msgLength % 64L);
        this.buffer[pos] = b;
        ++this.msgLength;
        if (pos == 63) {
            this.process(this.buffer, 0);
        }
    }

    @Override
    protected void engineUpdate(byte[] b, int offset, int len) {
        int pos = (int)(this.msgLength % 64L);
        int nbOfCharsToFillBuf = 64 - pos;
        int blkStart = 0;
        this.msgLength += (long)len;
        if (len >= nbOfCharsToFillBuf) {
            System.arraycopy(b, offset, this.buffer, pos, nbOfCharsToFillBuf);
            this.process(this.buffer, 0);
            blkStart = nbOfCharsToFillBuf;
            while (blkStart + 64 - 1 < len) {
                this.process(b, offset + blkStart);
                blkStart += 64;
            }
            pos = 0;
        }
        if (blkStart < len) {
            System.arraycopy(b, offset + blkStart, this.buffer, pos, len - blkStart);
        }
    }

    @Override
    protected byte[] engineDigest() {
        byte[] p = this.pad();
        this.engineUpdate(p, 0, p.length);
        byte[] digest = new byte[]{(byte)this.a, (byte)(this.a >>> 8), (byte)(this.a >>> 16), (byte)(this.a >>> 24), (byte)this.b, (byte)(this.b >>> 8), (byte)(this.b >>> 16), (byte)(this.b >>> 24), (byte)this.c, (byte)(this.c >>> 8), (byte)(this.c >>> 16), (byte)(this.c >>> 24), (byte)this.d, (byte)(this.d >>> 8), (byte)(this.d >>> 16), (byte)(this.d >>> 24)};
        this.engineReset();
        return digest;
    }

    @Override
    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        if (offset < 0 || offset + len >= buf.length) {
            throw new DigestException("Wrong offset or not enough space to store the digest");
        }
        int destLength = Math.min(len, 16);
        System.arraycopy(this.engineDigest(), 0, buf, offset, destLength);
        return destLength;
    }

    @Override
    protected void engineReset() {
        this.a = 1732584193;
        this.b = -271733879;
        this.c = -1732584194;
        this.d = 271733878;
        this.msgLength = 0L;
    }

    private byte[] pad() {
        int pos = (int)(this.msgLength % 64L);
        int padLength = pos < 56 ? 64 - pos : 128 - pos;
        byte[] pad = new byte[padLength];
        pad[0] = -128;
        long bits = this.msgLength << 3;
        int index = padLength - 8;
        for (int i = 0; i < 8; ++i) {
            pad[index++] = (byte)(bits >>> (i << 3));
        }
        return pad;
    }

    private void process(byte[] in, int offset) {
        int aa = this.a;
        int bb = this.b;
        int cc = this.c;
        int dd = this.d;
        int[] X = new int[16];
        for (int i = 0; i < 16; ++i) {
            X[i] = in[offset++] & 0xFF | (in[offset++] & 0xFF) << 8 | (in[offset++] & 0xFF) << 16 | (in[offset++] & 0xFF) << 24;
        }
        this.a += (this.b & this.c | ~this.b & this.d) + X[0];
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & this.b | ~this.a & this.c) + X[1];
        this.d = this.d << 7 | this.d >>> 25;
        this.c += (this.d & this.a | ~this.d & this.b) + X[2];
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c & this.d | ~this.c & this.a) + X[3];
        this.b = this.b << 19 | this.b >>> 13;
        this.a += (this.b & this.c | ~this.b & this.d) + X[4];
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & this.b | ~this.a & this.c) + X[5];
        this.d = this.d << 7 | this.d >>> 25;
        this.c += (this.d & this.a | ~this.d & this.b) + X[6];
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c & this.d | ~this.c & this.a) + X[7];
        this.b = this.b << 19 | this.b >>> 13;
        this.a += (this.b & this.c | ~this.b & this.d) + X[8];
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & this.b | ~this.a & this.c) + X[9];
        this.d = this.d << 7 | this.d >>> 25;
        this.c += (this.d & this.a | ~this.d & this.b) + X[10];
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c & this.d | ~this.c & this.a) + X[11];
        this.b = this.b << 19 | this.b >>> 13;
        this.a += (this.b & this.c | ~this.b & this.d) + X[12];
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & this.b | ~this.a & this.c) + X[13];
        this.d = this.d << 7 | this.d >>> 25;
        this.c += (this.d & this.a | ~this.d & this.b) + X[14];
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c & this.d | ~this.c & this.a) + X[15];
        this.b = this.b << 19 | this.b >>> 13;
        this.a += (this.b & (this.c | this.d) | this.c & this.d) + X[0] + 1518500249;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & (this.b | this.c) | this.b & this.c) + X[4] + 1518500249;
        this.d = this.d << 5 | this.d >>> 27;
        this.c += (this.d & (this.a | this.b) | this.a & this.b) + X[8] + 1518500249;
        this.c = this.c << 9 | this.c >>> 23;
        this.b += (this.c & (this.d | this.a) | this.d & this.a) + X[12] + 1518500249;
        this.b = this.b << 13 | this.b >>> 19;
        this.a += (this.b & (this.c | this.d) | this.c & this.d) + X[1] + 1518500249;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & (this.b | this.c) | this.b & this.c) + X[5] + 1518500249;
        this.d = this.d << 5 | this.d >>> 27;
        this.c += (this.d & (this.a | this.b) | this.a & this.b) + X[9] + 1518500249;
        this.c = this.c << 9 | this.c >>> 23;
        this.b += (this.c & (this.d | this.a) | this.d & this.a) + X[13] + 1518500249;
        this.b = this.b << 13 | this.b >>> 19;
        this.a += (this.b & (this.c | this.d) | this.c & this.d) + X[2] + 1518500249;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & (this.b | this.c) | this.b & this.c) + X[6] + 1518500249;
        this.d = this.d << 5 | this.d >>> 27;
        this.c += (this.d & (this.a | this.b) | this.a & this.b) + X[10] + 1518500249;
        this.c = this.c << 9 | this.c >>> 23;
        this.b += (this.c & (this.d | this.a) | this.d & this.a) + X[14] + 1518500249;
        this.b = this.b << 13 | this.b >>> 19;
        this.a += (this.b & (this.c | this.d) | this.c & this.d) + X[3] + 1518500249;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a & (this.b | this.c) | this.b & this.c) + X[7] + 1518500249;
        this.d = this.d << 5 | this.d >>> 27;
        this.c += (this.d & (this.a | this.b) | this.a & this.b) + X[11] + 1518500249;
        this.c = this.c << 9 | this.c >>> 23;
        this.b += (this.c & (this.d | this.a) | this.d & this.a) + X[15] + 1518500249;
        this.b = this.b << 13 | this.b >>> 19;
        this.a += (this.b ^ this.c ^ this.d) + X[0] + 1859775393;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a ^ this.b ^ this.c) + X[8] + 1859775393;
        this.d = this.d << 9 | this.d >>> 23;
        this.c += (this.d ^ this.a ^ this.b) + X[4] + 1859775393;
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c ^ this.d ^ this.a) + X[12] + 1859775393;
        this.b = this.b << 15 | this.b >>> 17;
        this.a += (this.b ^ this.c ^ this.d) + X[2] + 1859775393;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a ^ this.b ^ this.c) + X[10] + 1859775393;
        this.d = this.d << 9 | this.d >>> 23;
        this.c += (this.d ^ this.a ^ this.b) + X[6] + 1859775393;
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c ^ this.d ^ this.a) + X[14] + 1859775393;
        this.b = this.b << 15 | this.b >>> 17;
        this.a += (this.b ^ this.c ^ this.d) + X[1] + 1859775393;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a ^ this.b ^ this.c) + X[9] + 1859775393;
        this.d = this.d << 9 | this.d >>> 23;
        this.c += (this.d ^ this.a ^ this.b) + X[5] + 1859775393;
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c ^ this.d ^ this.a) + X[13] + 1859775393;
        this.b = this.b << 15 | this.b >>> 17;
        this.a += (this.b ^ this.c ^ this.d) + X[3] + 1859775393;
        this.a = this.a << 3 | this.a >>> 29;
        this.d += (this.a ^ this.b ^ this.c) + X[11] + 1859775393;
        this.d = this.d << 9 | this.d >>> 23;
        this.c += (this.d ^ this.a ^ this.b) + X[7] + 1859775393;
        this.c = this.c << 11 | this.c >>> 21;
        this.b += (this.c ^ this.d ^ this.a) + X[15] + 1859775393;
        this.b = this.b << 15 | this.b >>> 17;
        this.a += aa;
        this.b += bb;
        this.c += cc;
        this.d += dd;
    }
}

