/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.DynamicBuffer;
import com.maverick.util.EOLProcessor;
import java.io.IOException;
import java.io.InputStream;

class EOLProcessorInputStream
extends InputStream {
    EOLProcessor processor;
    InputStream in;
    DynamicBuffer buf = new DynamicBuffer();
    byte[] tmp = new byte[32768];

    public EOLProcessorInputStream(int inputStyle, int outputStyle, InputStream in) throws IOException {
        this.in = in;
        this.processor = new EOLProcessor(inputStyle, outputStyle, this.buf.getOutputStream());
    }

    @Override
    public int read() throws IOException {
        this.fillBuffer(1);
        return this.buf.getInputStream().read();
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.fillBuffer(len);
        return this.buf.getInputStream().read(b, off, len);
    }

    private void fillBuffer(int count) throws IOException {
        while (this.buf.available() < count) {
            int read = this.in.read(this.tmp);
            if (read == -1) {
                this.processor.close();
                this.buf.close();
                return;
            }
            this.processor.processBytes(this.tmp, 0, read);
        }
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }
}

