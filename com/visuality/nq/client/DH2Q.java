/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.ContextDescriptor;
import com.visuality.nq.client.File;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import java.util.Arrays;

class DH2Q
extends ContextDescriptor {
    int id = 2;
    int size = 32;

    DH2Q() {
    }

    public int pack(BufferWriter writer, File file) throws NqException {
        this.name = "DH2Q";
        int length = 0;
        int orign = writer.getOffset();
        writer.writeInt4(0);
        writer.writeInt2(16);
        writer.writeInt2((short)this.name.length());
        writer.writeInt2(0);
        int dataOffsetPosition = writer.getOffset();
        writer.writeInt2(0);
        writer.writeInt4(this.size);
        writer.writeBytes(this.name.getBytes(), this.name.length());
        writer.align(4, 8);
        short dataOffset = (short)(writer.getOffset() - orign);
        writer.writeInt4(0);
        writer.writeInt4(file.durableFlags);
        writer.writeZeros(8);
        file.durableHandle.write(writer);
        int position = writer.getOffset();
        writer.setOffset(dataOffsetPosition);
        writer.writeInt2(dataOffset);
        writer.setOffset(position);
        length = position - orign;
        return length;
    }

    boolean process(BufferReader reader, File file) throws NqException {
        byte[] name = new byte[4];
        reader.skip(16);
        reader.readBytes(name, name.length);
        boolean result = Arrays.equals(name, this.name.getBytes());
        if (result) {
            file.durableState = 3;
            file.durableFlags = 0;
            reader.skip(4);
            file.durableTimeout = reader.readInt4();
            file.durableFlags = reader.readInt4();
        }
        return result;
    }

    public int getLength() {
        return this.size;
    }
}

