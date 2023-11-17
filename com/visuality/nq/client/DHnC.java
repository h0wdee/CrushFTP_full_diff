/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.ContextDescriptor;
import com.visuality.nq.client.File;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;

class DHnC
extends ContextDescriptor {
    int id = 1;
    int size = 16;

    DHnC() {
    }

    public int pack(BufferWriter writer, File file) throws NqException {
        this.name = "DHnC";
        int length = 0;
        if (file.durableState == 3) {
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
            writer.writeBytes(file.fid, 16);
            int position = writer.getOffset();
            writer.setOffset(dataOffsetPosition);
            writer.writeInt2(dataOffset);
            writer.setOffset(position);
            length = position - orign;
        }
        return length;
    }

    public int getLength() {
        return this.size;
    }
}

