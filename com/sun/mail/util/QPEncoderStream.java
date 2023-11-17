/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QPEncoderStream
extends FilterOutputStream {
    private int count = 0;
    private int bytesPerLine;
    private boolean gotSpace = false;
    private boolean gotCR = false;
    private static final char[] hex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public QPEncoderStream(OutputStream out, int bytesPerLine) {
        super(out);
        this.bytesPerLine = bytesPerLine - 1;
    }

    public QPEncoderStream(OutputStream out) {
        this(out, 76);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; ++i) {
            this.write(b[off + i]);
        }
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    public void write(int c) throws IOException {
        c &= 0xFF;
        if (this.gotSpace) {
            if (c == 13 || c == 10) {
                this.output(32, true);
            } else {
                this.output(32, false);
            }
            this.gotSpace = false;
        }
        if (c == 13) {
            this.gotCR = true;
            this.outputCRLF();
        } else {
            if (c == 10) {
                if (!this.gotCR) {
                    this.outputCRLF();
                }
            } else if (c == 32) {
                this.gotSpace = true;
            } else if (c < 32 || c >= 127 || c == 61) {
                this.output(c, true);
            } else {
                this.output(c, false);
            }
            this.gotCR = false;
        }
    }

    public void flush() throws IOException {
        if (this.gotSpace) {
            this.output(32, true);
            this.gotSpace = false;
        }
        this.out.flush();
    }

    public void close() throws IOException {
        this.flush();
        this.out.close();
    }

    private void outputCRLF() throws IOException {
        this.out.write(13);
        this.out.write(10);
        this.count = 0;
    }

    protected void output(int c, boolean encode) throws IOException {
        if (encode) {
            if ((this.count += 3) > this.bytesPerLine) {
                this.out.write(61);
                this.out.write(13);
                this.out.write(10);
                this.count = 3;
            }
            this.out.write(61);
            this.out.write(hex[c >> 4]);
            this.out.write(hex[c & 0xF]);
        } else {
            if (++this.count > this.bytesPerLine) {
                this.out.write(61);
                this.out.write(13);
                this.out.write(10);
                this.count = 1;
            }
            this.out.write(c);
        }
    }
}

