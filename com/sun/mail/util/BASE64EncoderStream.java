/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BASE64EncoderStream
extends FilterOutputStream {
    private byte[] buffer = new byte[3];
    private int bufsize = 0;
    private byte[] outbuf;
    private int count = 0;
    private int bytesPerLine;
    private int lineLimit;
    private boolean noCRLF = false;
    private static byte[] newline = new byte[]{13, 10};
    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    public BASE64EncoderStream(OutputStream out, int bytesPerLine) {
        super(out);
        if (bytesPerLine == Integer.MAX_VALUE || bytesPerLine < 4) {
            this.noCRLF = true;
            bytesPerLine = 76;
        }
        this.bytesPerLine = bytesPerLine = bytesPerLine / 4 * 4;
        this.lineLimit = bytesPerLine / 4 * 3;
        if (this.noCRLF) {
            this.outbuf = new byte[bytesPerLine];
        } else {
            this.outbuf = new byte[bytesPerLine + 2];
            this.outbuf[bytesPerLine] = 13;
            this.outbuf[bytesPerLine + 1] = 10;
        }
    }

    public BASE64EncoderStream(OutputStream out) {
        this(out, 76);
    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
        int outlen;
        int end = off + len;
        while (this.bufsize != 0 && off < end) {
            this.write(b[off++]);
        }
        int blen = (this.bytesPerLine - this.count) / 4 * 3;
        if (off + blen <= end) {
            outlen = BASE64EncoderStream.encodedSize(blen);
            if (!this.noCRLF) {
                this.outbuf[outlen++] = 13;
                this.outbuf[outlen++] = 10;
            }
            this.out.write(BASE64EncoderStream.encode(b, off, blen, this.outbuf), 0, outlen);
            off += blen;
            this.count = 0;
        }
        while (off + this.lineLimit <= end) {
            this.out.write(BASE64EncoderStream.encode(b, off, this.lineLimit, this.outbuf));
            off += this.lineLimit;
        }
        if (off + 3 <= end) {
            blen = end - off;
            blen = blen / 3 * 3;
            outlen = BASE64EncoderStream.encodedSize(blen);
            this.out.write(BASE64EncoderStream.encode(b, off, blen, this.outbuf), 0, outlen);
            off += blen;
            this.count += outlen;
        }
        while (off < end) {
            this.write(b[off]);
            ++off;
        }
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    public synchronized void write(int c) throws IOException {
        this.buffer[this.bufsize++] = (byte)c;
        if (this.bufsize == 3) {
            this.encode();
            this.bufsize = 0;
        }
    }

    public synchronized void flush() throws IOException {
        if (this.bufsize > 0) {
            this.encode();
            this.bufsize = 0;
        }
        this.out.flush();
    }

    public synchronized void close() throws IOException {
        this.flush();
        if (this.count > 0 && !this.noCRLF) {
            this.out.write(newline);
            this.out.flush();
        }
        this.out.close();
    }

    private void encode() throws IOException {
        int osize = BASE64EncoderStream.encodedSize(this.bufsize);
        this.out.write(BASE64EncoderStream.encode(this.buffer, 0, this.bufsize, this.outbuf), 0, osize);
        this.count += osize;
        if (this.count >= this.bytesPerLine) {
            if (!this.noCRLF) {
                this.out.write(newline);
            }
            this.count = 0;
        }
    }

    public static byte[] encode(byte[] inbuf) {
        if (inbuf.length == 0) {
            return inbuf;
        }
        return BASE64EncoderStream.encode(inbuf, 0, inbuf.length, null);
    }

    private static byte[] encode(byte[] inbuf, int off, int size, byte[] outbuf) {
        int val;
        if (outbuf == null) {
            outbuf = new byte[BASE64EncoderStream.encodedSize(size)];
        }
        int inpos = off;
        int outpos = 0;
        while (size >= 3) {
            val = inbuf[inpos++] & 0xFF;
            val <<= 8;
            val |= inbuf[inpos++] & 0xFF;
            val <<= 8;
            outbuf[outpos + 3] = (byte)pem_array[(val |= inbuf[inpos++] & 0xFF) & 0x3F];
            outbuf[outpos + 2] = (byte)pem_array[(val >>= 6) & 0x3F];
            outbuf[outpos + 1] = (byte)pem_array[(val >>= 6) & 0x3F];
            outbuf[outpos + 0] = (byte)pem_array[(val >>= 6) & 0x3F];
            size -= 3;
            outpos += 4;
        }
        if (size == 1) {
            val = inbuf[inpos++] & 0xFF;
            outbuf[outpos + 3] = 61;
            outbuf[outpos + 2] = 61;
            outbuf[outpos + 1] = (byte)pem_array[(val <<= 4) & 0x3F];
            outbuf[outpos + 0] = (byte)pem_array[(val >>= 6) & 0x3F];
        } else if (size == 2) {
            val = inbuf[inpos++] & 0xFF;
            val <<= 8;
            val |= inbuf[inpos++] & 0xFF;
            outbuf[outpos + 3] = 61;
            outbuf[outpos + 2] = (byte)pem_array[(val <<= 2) & 0x3F];
            outbuf[outpos + 1] = (byte)pem_array[(val >>= 6) & 0x3F];
            outbuf[outpos + 0] = (byte)pem_array[(val >>= 6) & 0x3F];
        }
        return outbuf;
    }

    private static int encodedSize(int size) {
        return (size + 2) / 3 * 4;
    }
}

