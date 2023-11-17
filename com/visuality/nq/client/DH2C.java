/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.ContextDescriptor;
import com.visuality.nq.client.File;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;

class DH2C
extends ContextDescriptor {
    int id = 3;
    int size = 36;

    DH2C() {
    }

    public int pack(BufferWriter writer, File file) throws NqException {
        this.name = "DH2C";
        int length = 0;
        if (file.durableState == 3) {
            int origin = writer.getOffset();
            writer.writeInt4(0);
            writer.writeInt2(16);
            writer.writeInt2((short)this.name.length());
            writer.writeInt2(0);
            int dataOffsetPosition = writer.getOffset();
            writer.writeInt2(0);
            writer.writeInt4(this.size);
            writer.writeBytes(this.name.getBytes(), this.name.length());
            writer.align(4, 8);
            short dataOffset = (short)(writer.getOffset() - origin);
            writer.writeBytes(file.fid, 16);
            file.durableHandle.write(writer);
            writer.writeInt4(file.durableFlags);
            int position = writer.getOffset();
            writer.setOffset(dataOffsetPosition);
            writer.writeInt2(dataOffset);
            writer.setOffset(position);
            length = position - origin;
        }
        return length;
    }

    boolean process(BufferReader bufferReader, File file) {
        return true;
    }

    public int getLength() {
        return this.size;
    }
}

