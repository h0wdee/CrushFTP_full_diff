/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TextConversionStream
extends FilterOutputStream {
    public static final int TEXT_SYSTEM = 0;
    public static final int TEXT_WINDOWS = 1;
    public static final int TEXT_DOS = 1;
    public static final int TEXT_CRLF = 1;
    public static final int TEXT_UNIX = 2;
    public static final int TEXT_LF = 2;
    public static final int TEXT_MAC = 3;
    public static final int TEXT_CR = 3;
    public static final int TEXT_ALL = 4;
    byte[] lineEnding;
    String systemNL = System.getProperty("line.separator");
    boolean stripCR;
    boolean stripLF;
    boolean stripCRLF;
    boolean encounteredBinary = false;
    boolean lastCharacterWasCR = false;

    public TextConversionStream(int inputStyle, int outputStyle, OutputStream out) {
        super(out);
        switch (inputStyle) {
            case 1: {
                this.stripCR = false;
                this.stripLF = false;
                this.stripCRLF = true;
                break;
            }
            case 3: {
                this.stripCR = true;
                this.stripLF = false;
                this.stripCRLF = false;
                break;
            }
            case 2: {
                this.stripCR = false;
                this.stripLF = true;
                this.stripCRLF = false;
                break;
            }
            case 4: {
                this.stripCR = true;
                this.stripLF = true;
                this.stripCRLF = true;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown text style: " + outputStyle);
            }
        }
        switch (outputStyle) {
            case 0: {
                this.lineEnding = this.systemNL.getBytes();
                break;
            }
            case 1: {
                this.lineEnding = new byte[]{13, 10};
                break;
            }
            case 3: {
                this.lineEnding = new byte[]{13};
                break;
            }
            case 2: {
                this.lineEnding = new byte[]{10};
                break;
            }
            case 4: {
                throw new IllegalArgumentException("TEXT_ALL cannot be used for an output style");
            }
            default: {
                throw new IllegalArgumentException("Unknown text style: " + outputStyle);
            }
        }
    }

    public boolean hasBinary() {
        return this.encounteredBinary;
    }

    @Override
    public void write(int b) throws IOException {
        this.write(new byte[]{(byte)b});
    }

    @Override
    public void close() throws IOException {
        if (this.lastCharacterWasCR && !this.stripCR) {
            this.out.write(13);
        }
        super.close();
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        int b;
        BufferedInputStream bin = new BufferedInputStream(new ByteArrayInputStream(buf, off, len), 32768);
        while ((b = bin.read()) != -1) {
            if (b == 13) {
                if (this.stripCRLF) {
                    bin.mark(1);
                    int ch = bin.read();
                    if (ch == -1) {
                        this.lastCharacterWasCR = true;
                        break;
                    }
                    if (ch == 10) {
                        this.out.write(this.lineEnding);
                        continue;
                    }
                    bin.reset();
                    if (this.stripCR) {
                        this.out.write(this.lineEnding);
                        continue;
                    }
                    this.out.write(b);
                    continue;
                }
                if (this.stripCR) {
                    this.out.write(this.lineEnding);
                    continue;
                }
                this.out.write(b);
                continue;
            }
            if (b == 10) {
                if (this.lastCharacterWasCR) {
                    this.out.write(this.lineEnding);
                    this.lastCharacterWasCR = false;
                    continue;
                }
                if (this.stripLF) {
                    this.out.write(this.lineEnding);
                    continue;
                }
                this.out.write(b);
                continue;
            }
            if (this.lastCharacterWasCR) {
                if (this.stripCR) {
                    this.out.write(this.lineEnding);
                } else {
                    this.out.write(b);
                }
            }
            if (b != 116 && b != 12 && (b & 0xFF) < 32) {
                this.encounteredBinary = true;
            }
            this.out.write(b);
        }
    }

    public static void main(String[] args) {
        try {
            TextConversionStream t = new TextConversionStream(1, 3, new FileOutputStream("C:\\TEXT.txt"));
            t.write("1234567890\r".getBytes());
            t.write("\n01234567890\r\n".getBytes());
            t.write("\r\n12323445546657".getBytes());
            t.write("21344356545656\r".getBytes());
            t.close();
        }
        catch (Exception e) {
            System.out.println("RECIEVED IOException IN Ssh1Protocol.close:" + e.getMessage());
        }
    }
}

