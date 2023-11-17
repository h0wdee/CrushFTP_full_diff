/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.SshCipher;
import com.maverick.util.Arrays;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.UnsignedInteger64;
import java.io.IOException;

public class ChaCha20Poly1305
extends SshCipher {
    byte[] k1 = new byte[32];
    byte[] k2 = new byte[32];
    int mode;
    UnsignedInteger64 currentSequenceNo;

    public ChaCha20Poly1305() throws IOException {
        super("chacha20-poly1305@openssh.com", SecurityLevel.PARANOID);
    }

    @Override
    public void init(int mode, byte[] iv, byte[] keydata) throws IOException {
        this.mode = mode;
        System.arraycopy(keydata, 0, this.k2, 0, this.k2.length);
        System.arraycopy(keydata, 32, this.k1, 0, this.k1.length);
    }

    @Override
    public int getPriority() {
        return this.getSecurityLevel().ordinal() * 1000 + 1;
    }

    @Override
    public int getBlockSize() {
        return 8;
    }

    @Override
    public int getKeyLength() {
        return 64;
    }

    @Override
    public int getMacLength() {
        return 16;
    }

    @Override
    public boolean isMAC() {
        return true;
    }

    @Override
    public void transform(byte[] src, int start, byte[] dest, int offset, int len) throws IOException {
        try {
            if (this.mode == 1) {
                this.doDecrypt(src, start, dest, offset, len);
            } else {
                this.doEncrypt(src, start, dest, offset, len);
            }
        }
        catch (ChaCha20.WrongKeySizeException | ChaCha20.WrongNonceSizeException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private void doEncrypt(byte[] src, int start, byte[] dest, int offset, int len) throws ChaCha20.WrongKeySizeException, ChaCha20.WrongNonceSizeException, IllegalStateException, IOException {
        int payloadLength = 4 + len - 16;
        this.transformPayload(src, start, dest, offset, payloadLength);
        byte[] polykey = this.generatePoly1305Key();
        Poly1305 mac = new Poly1305();
        mac.init(polykey);
        mac.update(src, 0, payloadLength);
        byte[] expectedTag = this.generatePoly1305Tag(polykey, src, 0, payloadLength);
        System.arraycopy(expectedTag, 0, dest, payloadLength, expectedTag.length);
    }

    private void doDecrypt(byte[] src, int start, byte[] dest, int offset, int len) throws ChaCha20.WrongKeySizeException, ChaCha20.WrongNonceSizeException, IllegalStateException, IOException {
        byte[] tag = new byte[16];
        int payloadLength = 4 + len - 16;
        System.arraycopy(src, payloadLength, tag, 0, 16);
        byte[] polykey = this.generatePoly1305Key();
        byte[] expectedTag = this.generatePoly1305Tag(polykey, src, 0, payloadLength);
        if (!Arrays.areEqual(tag, expectedTag)) {
            throw new IOException("Corrupt authentication tag");
        }
        this.transformPayload(src, start, dest, offset, len);
    }

    private void transformPayload(byte[] src, int start, byte[] dst, int off, int len) throws ChaCha20.WrongKeySizeException, ChaCha20.WrongNonceSizeException {
        ChaCha20 cha = new ChaCha20(this.k2, this.currentSequenceNo.toByteArray(), 1);
        cha.encrypt(dst, off, src, start, len);
    }

    private byte[] generatePoly1305Key() throws ChaCha20.WrongKeySizeException, ChaCha20.WrongNonceSizeException {
        byte[] polykey = new byte[this.k2.length];
        ChaCha20 cha = new ChaCha20(this.k2, this.currentSequenceNo.toByteArray(), 0);
        cha.encrypt(polykey, 0, polykey, 0, polykey.length);
        return polykey;
    }

    private byte[] generatePoly1305Tag(byte[] polykey, byte[] src, int off, int len) throws IllegalStateException, IOException {
        Poly1305 mac = new Poly1305();
        mac.init(polykey);
        mac.update(src, off, len);
        byte[] expectedTag = new byte[16];
        mac.doFinal(expectedTag, 0);
        return expectedTag;
    }

    @Override
    public String getProviderName() {
        return "JADAPTIVE";
    }

    public long readPacketLength(byte[] encoded, UnsignedInteger64 sequenceNo) throws IOException {
        try {
            this.currentSequenceNo = sequenceNo;
            ChaCha20 cha = new ChaCha20(this.k1, sequenceNo.toByteArray(), 0);
            byte[] tmp = new byte[4];
            cha.encrypt(tmp, 0, encoded, 0, 4);
            return ByteArrayReader.readInt(tmp, 0);
        }
        catch (ChaCha20.WrongKeySizeException | ChaCha20.WrongNonceSizeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public byte[] writePacketLength(int length, UnsignedInteger64 sequenceNo) throws IOException {
        try {
            this.currentSequenceNo = sequenceNo;
            ChaCha20 cha = new ChaCha20(this.k1, sequenceNo.toByteArray(), 0);
            byte[] tmp = new byte[4];
            cha.encrypt(tmp, 0, ByteArrayWriter.encodeInt(length), 0, 4);
            return tmp;
        }
        catch (ChaCha20.WrongKeySizeException | ChaCha20.WrongNonceSizeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected static int littleEndianToInt(byte[] bs, int i) {
        return bs[i] & 0xFF | (bs[i + 1] & 0xFF) << 8 | (bs[i + 2] & 0xFF) << 16 | (bs[i + 3] & 0xFF) << 24;
    }

    protected static void intToLittleEndian(int n, byte[] bs, int off) {
        bs[off] = (byte)n;
        bs[++off] = (byte)(n >>> 8);
        bs[++off] = (byte)(n >>> 16);
        bs[++off] = (byte)(n >>> 24);
    }

    public static class Poly1305 {
        private static final int BLOCK_SIZE = 16;
        private final byte[] singleByte = new byte[1];
        private int r0;
        private int r1;
        private int r2;
        private int r3;
        private int r4;
        private int s1;
        private int s2;
        private int s3;
        private int s4;
        private int k0;
        private int k1;
        private int k2;
        private int k3;
        private final byte[] currentBlock = new byte[16];
        private int currentBlockOffset = 0;
        private int h0;
        private int h1;
        private int h2;
        private int h3;
        private int h4;

        public void init(byte[] key) throws IllegalArgumentException {
            this.setKey(key);
            this.reset();
        }

        private void setKey(byte[] key) {
            if (key.length != 32) {
                throw new IllegalArgumentException("Poly1305 key must be 256 bits.");
            }
            int t0 = ChaCha20Poly1305.littleEndianToInt(key, 0);
            int t1 = ChaCha20Poly1305.littleEndianToInt(key, 4);
            int t2 = ChaCha20Poly1305.littleEndianToInt(key, 8);
            int t3 = ChaCha20Poly1305.littleEndianToInt(key, 12);
            this.r0 = t0 & 0x3FFFFFF;
            this.r1 = (t0 >>> 26 | t1 << 6) & 0x3FFFF03;
            this.r2 = (t1 >>> 20 | t2 << 12) & 0x3FFC0FF;
            this.r3 = (t2 >>> 14 | t3 << 18) & 0x3F03FFF;
            this.r4 = t3 >>> 8 & 0xFFFFF;
            this.s1 = this.r1 * 5;
            this.s2 = this.r2 * 5;
            this.s3 = this.r3 * 5;
            this.s4 = this.r4 * 5;
            byte[] kBytes = key;
            int kOff = 16;
            this.k0 = ChaCha20Poly1305.littleEndianToInt(kBytes, kOff + 0);
            this.k1 = ChaCha20Poly1305.littleEndianToInt(kBytes, kOff + 4);
            this.k2 = ChaCha20Poly1305.littleEndianToInt(kBytes, kOff + 8);
            this.k3 = ChaCha20Poly1305.littleEndianToInt(kBytes, kOff + 12);
        }

        public String getAlgorithmName() {
            return "Poly1305";
        }

        public int getMacSize() {
            return 16;
        }

        public void update(byte in) throws IOException, IllegalStateException {
            this.singleByte[0] = in;
            this.update(this.singleByte, 0, 1);
        }

        public void update(byte[] in, int inOff, int len) throws IOException, IllegalStateException {
            int copied = 0;
            while (len > copied) {
                if (this.currentBlockOffset == 16) {
                    this.processBlock();
                    this.currentBlockOffset = 0;
                }
                int toCopy = Math.min(len - copied, 16 - this.currentBlockOffset);
                System.arraycopy(in, copied + inOff, this.currentBlock, this.currentBlockOffset, toCopy);
                copied += toCopy;
                this.currentBlockOffset += toCopy;
            }
        }

        private void processBlock() {
            if (this.currentBlockOffset < 16) {
                this.currentBlock[this.currentBlockOffset] = 1;
                for (int i = this.currentBlockOffset + 1; i < 16; ++i) {
                    this.currentBlock[i] = 0;
                }
            }
            long t0 = 0xFFFFFFFFL & (long)ChaCha20Poly1305.littleEndianToInt(this.currentBlock, 0);
            long t1 = 0xFFFFFFFFL & (long)ChaCha20Poly1305.littleEndianToInt(this.currentBlock, 4);
            long t2 = 0xFFFFFFFFL & (long)ChaCha20Poly1305.littleEndianToInt(this.currentBlock, 8);
            long t3 = 0xFFFFFFFFL & (long)ChaCha20Poly1305.littleEndianToInt(this.currentBlock, 12);
            this.h0 = (int)((long)this.h0 + (t0 & 0x3FFFFFFL));
            this.h1 = (int)((long)this.h1 + ((t1 << 32 | t0) >>> 26 & 0x3FFFFFFL));
            this.h2 = (int)((long)this.h2 + ((t2 << 32 | t1) >>> 20 & 0x3FFFFFFL));
            this.h3 = (int)((long)this.h3 + ((t3 << 32 | t2) >>> 14 & 0x3FFFFFFL));
            this.h4 = (int)((long)this.h4 + (t3 >>> 8));
            if (this.currentBlockOffset == 16) {
                this.h4 += 0x1000000;
            }
            long tp0 = Poly1305.mul32x32_64(this.h0, this.r0) + Poly1305.mul32x32_64(this.h1, this.s4) + Poly1305.mul32x32_64(this.h2, this.s3) + Poly1305.mul32x32_64(this.h3, this.s2) + Poly1305.mul32x32_64(this.h4, this.s1);
            long tp1 = Poly1305.mul32x32_64(this.h0, this.r1) + Poly1305.mul32x32_64(this.h1, this.r0) + Poly1305.mul32x32_64(this.h2, this.s4) + Poly1305.mul32x32_64(this.h3, this.s3) + Poly1305.mul32x32_64(this.h4, this.s2);
            long tp2 = Poly1305.mul32x32_64(this.h0, this.r2) + Poly1305.mul32x32_64(this.h1, this.r1) + Poly1305.mul32x32_64(this.h2, this.r0) + Poly1305.mul32x32_64(this.h3, this.s4) + Poly1305.mul32x32_64(this.h4, this.s3);
            long tp3 = Poly1305.mul32x32_64(this.h0, this.r3) + Poly1305.mul32x32_64(this.h1, this.r2) + Poly1305.mul32x32_64(this.h2, this.r1) + Poly1305.mul32x32_64(this.h3, this.r0) + Poly1305.mul32x32_64(this.h4, this.s4);
            long tp4 = Poly1305.mul32x32_64(this.h0, this.r4) + Poly1305.mul32x32_64(this.h1, this.r3) + Poly1305.mul32x32_64(this.h2, this.r2) + Poly1305.mul32x32_64(this.h3, this.r1) + Poly1305.mul32x32_64(this.h4, this.r0);
            this.h0 = (int)tp0 & 0x3FFFFFF;
            this.h1 = (int)(tp1 += tp0 >>> 26) & 0x3FFFFFF;
            this.h2 = (int)(tp2 += tp1 >>> 26) & 0x3FFFFFF;
            this.h3 = (int)(tp3 += tp2 >>> 26) & 0x3FFFFFF;
            this.h4 = (int)(tp4 += tp3 >>> 26) & 0x3FFFFFF;
            this.h0 += (int)(tp4 >>> 26) * 5;
            this.h1 += this.h0 >>> 26;
            this.h0 &= 0x3FFFFFF;
        }

        public int doFinal(byte[] out, int outOff) throws IOException, IllegalStateException {
            if (outOff + 16 > out.length) {
                throw new IOException("Output buffer is too short.");
            }
            if (this.currentBlockOffset > 0) {
                this.processBlock();
            }
            this.h1 += this.h0 >>> 26;
            this.h0 &= 0x3FFFFFF;
            this.h2 += this.h1 >>> 26;
            this.h1 &= 0x3FFFFFF;
            this.h3 += this.h2 >>> 26;
            this.h2 &= 0x3FFFFFF;
            this.h4 += this.h3 >>> 26;
            this.h3 &= 0x3FFFFFF;
            this.h0 += (this.h4 >>> 26) * 5;
            this.h4 &= 0x3FFFFFF;
            this.h1 += this.h0 >>> 26;
            this.h0 &= 0x3FFFFFF;
            int g0 = this.h0 + 5;
            int b = g0 >>> 26;
            g0 &= 0x3FFFFFF;
            int g1 = this.h1 + b;
            b = g1 >>> 26;
            g1 &= 0x3FFFFFF;
            int g2 = this.h2 + b;
            b = g2 >>> 26;
            g2 &= 0x3FFFFFF;
            int g3 = this.h3 + b;
            b = g3 >>> 26;
            g3 &= 0x3FFFFFF;
            int g4 = this.h4 + b - 0x4000000;
            b = (g4 >>> 31) - 1;
            int nb = ~b;
            this.h0 = this.h0 & nb | g0 & b;
            this.h1 = this.h1 & nb | g1 & b;
            this.h2 = this.h2 & nb | g2 & b;
            this.h3 = this.h3 & nb | g3 & b;
            this.h4 = this.h4 & nb | g4 & b;
            long f0 = ((long)(this.h0 | this.h1 << 26) & 0xFFFFFFFFL) + (0xFFFFFFFFL & (long)this.k0);
            long f1 = ((long)(this.h1 >>> 6 | this.h2 << 20) & 0xFFFFFFFFL) + (0xFFFFFFFFL & (long)this.k1);
            long f2 = ((long)(this.h2 >>> 12 | this.h3 << 14) & 0xFFFFFFFFL) + (0xFFFFFFFFL & (long)this.k2);
            long f3 = ((long)(this.h3 >>> 18 | this.h4 << 8) & 0xFFFFFFFFL) + (0xFFFFFFFFL & (long)this.k3);
            ChaCha20Poly1305.intToLittleEndian((int)f0, out, outOff);
            ChaCha20Poly1305.intToLittleEndian((int)(f1 += f0 >>> 32), out, outOff + 4);
            ChaCha20Poly1305.intToLittleEndian((int)(f2 += f1 >>> 32), out, outOff + 8);
            ChaCha20Poly1305.intToLittleEndian((int)(f3 += f2 >>> 32), out, outOff + 12);
            this.reset();
            return 16;
        }

        public void reset() {
            this.currentBlockOffset = 0;
            this.h4 = 0;
            this.h3 = 0;
            this.h2 = 0;
            this.h1 = 0;
            this.h0 = 0;
        }

        private static final long mul32x32_64(int i1, int i2) {
            return ((long)i1 & 0xFFFFFFFFL) * (long)i2;
        }
    }

    public static class ChaCha20 {
        public static final int KEY_SIZE = 32;
        public static final int NONCE_SIZE_REF = 8;
        public static final int NONCE_SIZE_IETF = 12;
        private int[] matrix = new int[16];

        protected int ROTATE(int v, int c) {
            return v << c | v >>> 32 - c;
        }

        protected void quarterRound(int[] x, int a, int b, int c, int d) {
            int n = a;
            x[n] = x[n] + x[b];
            x[d] = this.ROTATE(x[d] ^ x[a], 16);
            int n2 = c;
            x[n2] = x[n2] + x[d];
            x[b] = this.ROTATE(x[b] ^ x[c], 12);
            int n3 = a;
            x[n3] = x[n3] + x[b];
            x[d] = this.ROTATE(x[d] ^ x[a], 8);
            int n4 = c;
            x[n4] = x[n4] + x[d];
            x[b] = this.ROTATE(x[b] ^ x[c], 7);
        }

        public ChaCha20(byte[] key, byte[] nonce, int counter) throws WrongKeySizeException, WrongNonceSizeException {
            if (key.length != 32) {
                throw new WrongKeySizeException();
            }
            this.matrix[0] = 1634760805;
            this.matrix[1] = 857760878;
            this.matrix[2] = 2036477234;
            this.matrix[3] = 1797285236;
            this.matrix[4] = ChaCha20Poly1305.littleEndianToInt(key, 0);
            this.matrix[5] = ChaCha20Poly1305.littleEndianToInt(key, 4);
            this.matrix[6] = ChaCha20Poly1305.littleEndianToInt(key, 8);
            this.matrix[7] = ChaCha20Poly1305.littleEndianToInt(key, 12);
            this.matrix[8] = ChaCha20Poly1305.littleEndianToInt(key, 16);
            this.matrix[9] = ChaCha20Poly1305.littleEndianToInt(key, 20);
            this.matrix[10] = ChaCha20Poly1305.littleEndianToInt(key, 24);
            this.matrix[11] = ChaCha20Poly1305.littleEndianToInt(key, 28);
            if (nonce.length == 8) {
                this.matrix[12] = counter;
                this.matrix[13] = 0;
                this.matrix[14] = ChaCha20Poly1305.littleEndianToInt(nonce, 0);
                this.matrix[15] = ChaCha20Poly1305.littleEndianToInt(nonce, 4);
            } else if (nonce.length == 12) {
                this.matrix[12] = counter;
                this.matrix[13] = ChaCha20Poly1305.littleEndianToInt(nonce, 0);
                this.matrix[14] = ChaCha20Poly1305.littleEndianToInt(nonce, 4);
                this.matrix[15] = ChaCha20Poly1305.littleEndianToInt(nonce, 8);
            } else {
                throw new WrongNonceSizeException();
            }
        }

        public void encrypt(byte[] dst, int doff, byte[] src, int soff, int len) {
            int[] x = new int[16];
            byte[] output = new byte[64];
            int dpos = 0;
            int spos = 0;
            while (len > 0) {
                int i = 16;
                while (i-- > 0) {
                    x[i] = this.matrix[i];
                }
                for (i = 20; i > 0; i -= 2) {
                    this.quarterRound(x, 0, 4, 8, 12);
                    this.quarterRound(x, 1, 5, 9, 13);
                    this.quarterRound(x, 2, 6, 10, 14);
                    this.quarterRound(x, 3, 7, 11, 15);
                    this.quarterRound(x, 0, 5, 10, 15);
                    this.quarterRound(x, 1, 6, 11, 12);
                    this.quarterRound(x, 2, 7, 8, 13);
                    this.quarterRound(x, 3, 4, 9, 14);
                }
                i = 16;
                while (i-- > 0) {
                    int n = i;
                    x[n] = x[n] + this.matrix[i];
                }
                i = 16;
                while (i-- > 0) {
                    ChaCha20Poly1305.intToLittleEndian(x[i], output, 4 * i);
                }
                this.matrix[12] = this.matrix[12] + 1;
                if (this.matrix[12] <= 0) {
                    this.matrix[13] = this.matrix[13] + 1;
                }
                if (len <= 64) {
                    i = len;
                    while (i-- > 0) {
                        dst[doff + i + dpos] = (byte)(src[soff + i + spos] ^ output[i]);
                    }
                    break;
                }
                i = 64;
                while (i-- > 0) {
                    dst[doff + i + dpos] = (byte)(src[soff + i + spos] ^ output[i]);
                }
                len -= 64;
                spos += 64;
                dpos += 64;
            }
        }

        public class WrongKeySizeException
        extends Exception {
            private static final long serialVersionUID = -290509589749955895L;
        }

        public class WrongNonceSizeException
        extends Exception {
            private static final long serialVersionUID = 2687731889587117531L;
        }
    }
}

