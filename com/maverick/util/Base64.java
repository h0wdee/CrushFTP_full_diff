/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Base64 {
    public static final boolean ENCODE = true;
    public static final boolean DECODE = false;
    private static final int MAX_LINE_LENGTH = 64;
    private static final byte EQUALS_SIGN = 61;
    private static final byte NEW_LINE = 10;
    private static final byte[] ALPHABET = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    private static final byte[] DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9};
    private static final byte BAD_ENCODING = -9;
    private static final byte white_SPACE_ENC = -5;
    private static final byte EQUALS_SIGN_ENC = -1;

    private Base64() {
    }

    public static byte[] decode(String s) {
        byte[] bytes = s.getBytes();
        return Base64.decode(bytes, 0, bytes.length);
    }

    public static byte[] decode(byte[] source, int off, int len) {
        int len34 = len * 3 / 4;
        byte[] outBuff = new byte[len34];
        int outBuffPosn = 0;
        byte[] b4 = new byte[4];
        int b4Posn = 0;
        int i = 0;
        byte sbiCrop = 0;
        byte sbiDecode = 0;
        for (i = off; i < len; ++i) {
            sbiCrop = (byte)(source[i] & 0x7F);
            sbiDecode = DECODABET[sbiCrop];
            if (sbiDecode >= -5) {
                if (sbiDecode < -1) continue;
                b4[b4Posn++] = sbiCrop;
                if (b4Posn <= 3) continue;
                outBuffPosn += Base64.decode4to3(b4, 0, outBuff, outBuffPosn);
                b4Posn = 0;
                if (sbiCrop != 61) continue;
                break;
            }
            System.err.println("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
            return null;
        }
        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object decodeToObject(String encodedObject) {
        byte[] objBytes = Base64.decode(encodedObject);
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(objBytes);
            ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            return object;
        }
        catch (IOException e) {
            Object var5_9 = null;
            return var5_9;
        }
        catch (ClassNotFoundException e) {
            Object var5_10 = null;
            return var5_10;
        }
        finally {
            try {
                bais.close();
            }
            catch (Exception exception) {}
            try {
                ois.close();
            }
            catch (Exception exception) {}
        }
    }

    public static String decodeToString(String s) {
        return new String(Base64.decode(s));
    }

    public static String encodeBytes(byte[] source, boolean ignoreMaxLineLength) {
        return Base64.encodeBytes(source, 0, source.length, ignoreMaxLineLength);
    }

    public static String encodeBytes(byte[] source, int off, int len, boolean ignoreMaxLineLength) {
        int len43 = len * 4 / 3;
        byte[] outBuff = new byte[len43 + (len % 3 > 0 ? 4 : 0) + len43 / 64];
        int d = 0;
        int e = 0;
        int len2 = len - 2;
        int lineLength = 0;
        while (d < len2) {
            Base64.encode3to4(source, d + off, 3, outBuff, e);
            if (!ignoreMaxLineLength && (lineLength += 4) == 64) {
                outBuff[e + 4] = 10;
                ++e;
                lineLength = 0;
            }
            d += 3;
            e += 4;
        }
        if (d < len) {
            Base64.encode3to4(source, d + off, len - d, outBuff, e);
            e += 4;
        }
        return new String(outBuff, 0, e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String encodeObject(Serializable serializableObject) {
        ByteArrayOutputStream baos = null;
        java.io.OutputStream b64os = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            b64os = new OutputStream(baos, true);
            oos = new ObjectOutputStream(b64os);
            oos.writeObject(serializableObject);
        }
        catch (IOException e) {
            String string = null;
            return string;
        }
        finally {
            try {
                oos.close();
            }
            catch (Exception exception) {}
            try {
                b64os.close();
            }
            catch (Exception exception) {}
            try {
                baos.close();
            }
            catch (Exception exception) {}
        }
        return new String(baos.toByteArray());
    }

    public static String encodeString(String s, boolean ignoreMaxLineLength) {
        return Base64.encodeBytes(s.getBytes(), ignoreMaxLineLength);
    }

    private static byte[] decode4to3(byte[] fourBytes) {
        byte[] outBuff1 = new byte[3];
        int count = Base64.decode4to3(fourBytes, 0, outBuff1, 0);
        byte[] outBuff2 = new byte[count];
        for (int i = 0; i < count; ++i) {
            outBuff2[i] = outBuff1[i];
        }
        return outBuff2;
    }

    private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
        if (source[srcOffset + 2] == 61) {
            int outBuff = DECODABET[source[srcOffset]] << 24 >>> 6 | DECODABET[source[srcOffset + 1]] << 24 >>> 12;
            destination[destOffset] = (byte)(outBuff >>> 16);
            return 1;
        }
        if (source[srcOffset + 3] == 61) {
            int outBuff = DECODABET[source[srcOffset]] << 24 >>> 6 | DECODABET[source[srcOffset + 1]] << 24 >>> 12 | DECODABET[source[srcOffset + 2]] << 24 >>> 18;
            destination[destOffset] = (byte)(outBuff >>> 16);
            destination[destOffset + 1] = (byte)(outBuff >>> 8);
            return 2;
        }
        int outBuff = DECODABET[source[srcOffset]] << 24 >>> 6 | DECODABET[source[srcOffset + 1]] << 24 >>> 12 | DECODABET[source[srcOffset + 2]] << 24 >>> 18 | DECODABET[source[srcOffset + 3]] << 24 >>> 24;
        destination[destOffset] = (byte)(outBuff >> 16);
        destination[destOffset + 1] = (byte)(outBuff >> 8);
        destination[destOffset + 2] = (byte)outBuff;
        return 3;
    }

    private static byte[] encode3to4(byte[] threeBytes, int numSigBytes) {
        byte[] dest = new byte[4];
        Base64.encode3to4(threeBytes, 0, numSigBytes, dest, 0);
        return dest;
    }

    private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset) {
        int inBuff = (numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0) | (numSigBytes > 1 ? source[srcOffset + 1] << 24 >>> 16 : 0) | (numSigBytes > 2 ? source[srcOffset + 2] << 24 >>> 24 : 0);
        switch (numSigBytes) {
            case 3: {
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3F];
                destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 0x3F];
                destination[destOffset + 3] = ALPHABET[inBuff & 0x3F];
                return destination;
            }
            case 2: {
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3F];
                destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 0x3F];
                destination[destOffset + 3] = 61;
                return destination;
            }
            case 1: {
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3F];
                destination[destOffset + 2] = 61;
                destination[destOffset + 3] = 61;
                return destination;
            }
        }
        return destination;
    }

    public static class OutputStream
    extends FilterOutputStream {
        private byte[] buffer;
        private boolean encode;
        private int bufferLength;
        private int lineLength;
        private int position;

        public OutputStream(java.io.OutputStream out) {
            this(out, true);
        }

        public OutputStream(java.io.OutputStream out, boolean encode) {
            super(out);
            this.encode = encode;
            this.bufferLength = encode ? 3 : 4;
            this.buffer = new byte[this.bufferLength];
            this.position = 0;
            this.lineLength = 0;
        }

        @Override
        public void close() throws IOException {
            this.flush();
            super.close();
            this.out.close();
            this.buffer = null;
            this.out = null;
        }

        @Override
        public void flush() throws IOException {
            if (this.position > 0) {
                if (this.encode) {
                    this.out.write(Base64.encode3to4(this.buffer, this.position));
                } else {
                    throw new IOException("Base64 input not properly padded.");
                }
            }
            super.flush();
            this.out.flush();
        }

        @Override
        public void write(int theByte) throws IOException {
            this.buffer[this.position++] = (byte)theByte;
            if (this.position >= this.bufferLength) {
                if (this.encode) {
                    this.out.write(Base64.encode3to4(this.buffer, this.bufferLength));
                    this.lineLength += 4;
                    if (this.lineLength >= 64) {
                        this.out.write(10);
                        this.lineLength = 0;
                    }
                } else {
                    this.out.write(Base64.decode4to3(this.buffer));
                }
                this.position = 0;
            }
        }

        @Override
        public void write(byte[] theBytes, int off, int len) throws IOException {
            for (int i = 0; i < len; ++i) {
                this.write(theBytes[off + i]);
            }
        }
    }

    public static class InputStream
    extends FilterInputStream {
        private byte[] buffer;
        private boolean encode;
        private int bufferLength;
        private int numSigBytes;
        private int position;

        public InputStream(java.io.InputStream in) {
            this(in, false);
        }

        public InputStream(java.io.InputStream in, boolean encode) {
            super(in);
            this.encode = encode;
            this.bufferLength = encode ? 4 : 3;
            this.buffer = new byte[this.bufferLength];
            this.position = -1;
        }

        @Override
        public int read() throws IOException {
            if (this.position < 0) {
                if (this.encode) {
                    byte[] b3 = new byte[3];
                    this.numSigBytes = 0;
                    for (int i = 0; i < 3; ++i) {
                        try {
                            int b = this.in.read();
                            if (b < 0) continue;
                            b3[i] = (byte)b;
                            ++this.numSigBytes;
                            continue;
                        }
                        catch (IOException e) {
                            if (i != 0) continue;
                            throw e;
                        }
                    }
                    if (this.numSigBytes > 0) {
                        Base64.encode3to4(b3, 0, this.numSigBytes, this.buffer, 0);
                        this.position = 0;
                    }
                } else {
                    byte[] b4 = new byte[4];
                    int i = 0;
                    for (i = 0; i < 4; ++i) {
                        int b = 0;
                        while ((b = this.in.read()) >= 0 && DECODABET[b & 0x7F] < -5) {
                        }
                        if (b < 0) break;
                        b4[i] = (byte)b;
                    }
                    if (i == 4) {
                        this.numSigBytes = Base64.decode4to3(b4, 0, this.buffer, 0);
                        this.position = 0;
                    }
                }
            }
            if (this.position >= 0) {
                if (!this.encode && this.position >= this.numSigBytes) {
                    return -1;
                }
                byte b = this.buffer[this.position++];
                if (this.position >= this.bufferLength) {
                    this.position = -1;
                }
                return b;
            }
            return -1;
        }

        @Override
        public int read(byte[] dest, int off, int len) throws IOException {
            int i;
            for (i = 0; i < len; ++i) {
                int b = this.read();
                if (b < 0) {
                    return -1;
                }
                dest[off + i] = (byte)b;
            }
            return i;
        }
    }
}

