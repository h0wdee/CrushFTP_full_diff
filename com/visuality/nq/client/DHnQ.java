/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.ContextDescriptor;
import com.visuality.nq.client.File;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;

class DHnQ
extends ContextDescriptor {
    int id = 0;
    int size = 16;

    DHnQ() {
    }

    public int pack(BufferWriter writer, File file) throws NqException {
        this.name = "DHnQ";
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
        writer.writeUuid(file.durableHandle);
        int position = writer.getOffset();
        writer.setOffset(dataOffsetPosition);
        writer.writeInt2(dataOffset);
        writer.setOffset(position);
        length = position - orign;
        return length;
    }

    public int getLength() {
        return 40;
    }
}

