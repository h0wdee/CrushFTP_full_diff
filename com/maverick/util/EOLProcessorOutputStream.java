/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.EOLProcessor;
import java.io.IOException;
import java.io.OutputStream;

class EOLProcessorOutputStream
extends OutputStream {
    EOLProcessor processor;

    public EOLProcessorOutputStream(int inputStyle, int outputStyle, OutputStream out) throws IOException {
        this.processor = new EOLProcessor(inputStyle, outputStyle, out);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.processor.processBytes(buf, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.processor.processBytes(new byte[]{(byte)b}, 0, 1);
    }

    @Override
    public void close() throws IOException {
        this.processor.close();
    }
}

