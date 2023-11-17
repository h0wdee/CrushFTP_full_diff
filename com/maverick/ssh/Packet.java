/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.util.ByteArrayWriter;
import java.io.IOException;

public class Packet
extends ByteArrayWriter {
    int markedPosition = -1;

    public Packet() throws IOException {
        this(35000);
    }

    public Packet(int size) throws IOException {
        super(size + 4);
        this.writeInt(0);
    }

    public int setPosition(int pos) {
        int count = this.count;
        this.count = pos;
        return count;
    }

    public int position() {
        return this.count;
    }

    public void finish() {
        this.buf[0] = (byte)(this.count - 4 >> 24);
        this.buf[1] = (byte)(this.count - 4 >> 16);
        this.buf[2] = (byte)(this.count - 4 >> 8);
        this.buf[3] = (byte)(this.count - 4);
    }

    @Override
    public void reset() {
        super.reset();
        try {
            this.writeInt(0);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

