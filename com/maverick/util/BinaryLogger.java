/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BinaryLogger {
    RandomAccessFile file;
    FileChannel channel;

    public BinaryLogger(String name) throws FileNotFoundException {
        this.file = new RandomAccessFile(new File(name), "rw");
        this.channel = this.file.getChannel();
    }

    public void log(byte[] buf) throws IOException {
        this.log(buf, 0, buf.length);
    }

    public void log(byte[] buf, int off, int len) throws IOException {
        this.channel.write(ByteBuffer.wrap(buf, off, len));
    }

    public void log(ByteBuffer buf) throws IOException {
        this.channel.write(buf);
    }

    public void close() {
        try {
            this.channel.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        try {
            this.file.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

