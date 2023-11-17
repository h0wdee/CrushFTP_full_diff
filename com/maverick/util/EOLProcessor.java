/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.EOLProcessorInputStream;
import com.maverick.util.EOLProcessorOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EOLProcessor {
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
    boolean stripCR = false;
    boolean stripLF = false;
    boolean stripCRLF = false;
    boolean encounteredBinary = false;
    boolean lastCharacterWasCR = false;
    OutputStream out;

    public EOLProcessor(int inputStyle, int outputStyle, OutputStream out) throws IOException {
        this.out = new BufferedOutputStream(out);
        switch (inputStyle) {
            case 1: {
                this.stripCRLF = true;
                break;
            }
            case 3: {
                this.stripCR = true;
                break;
            }
            case 2: {
                this.stripLF = true;
                break;
            }
            case 4: {
                this.stripCR = true;
                this.stripLF = true;
                this.stripCRLF = true;
                break;
            }
            case 0: {
                byte[] tmp = this.systemNL.getBytes();
                if (tmp.length == 2 && tmp[0] == 13 && tmp[1] == 10) {
                    this.stripCRLF = true;
                    break;
                }
                if (tmp.length == 1 && tmp[0] == 13) {
                    this.stripCR = true;
                    break;
                }
                if (tmp.length == 1 && tmp[0] == 10) {
                    this.stripLF = true;
                    break;
                }
                throw new IOException("Unsupported system EOL mode");
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

    public void close() throws IOException {
        if (this.lastCharacterWasCR) {
            if (this.stripCR) {
                this.out.write(this.lineEnding);
            } else {
                this.out.write(13);
            }
        }
        this.out.close();
    }

    public void processBytes(byte[] buf, int off, int len) throws IOException {
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
                    this.out.write(13);
                }
            }
            this.lastCharacterWasCR = false;
            if (b != 116 && b != 12 && (b & 0xFF) < 32) {
                this.encounteredBinary = true;
            }
            this.out.write(b);
        }
        this.out.flush();
    }

    public static OutputStream createOutputStream(int inputStyle, int outputStyle, OutputStream out) throws IOException {
        return new EOLProcessorOutputStream(inputStyle, outputStyle, out);
    }

    public static InputStream createInputStream(int inputStyle, int outputStyle, InputStream in) throws IOException {
        return new EOLProcessorInputStream(inputStyle, outputStyle, in);
    }
}

