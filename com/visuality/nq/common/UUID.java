/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbSerializable;
import com.visuality.nq.common.TraceLog;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class UUID
implements SmbSerializable {
    public int timeLow;
    public short timeMid;
    public short timeHiVersion;
    public byte[] clockSeq = new byte[2];
    public byte[] node = new byte[6];

    public UUID(int timeLow, int timeMid, int timeHiVersion, byte[] clockSeq, byte[] node) {
        this.timeLow = timeLow;
        this.timeMid = (short)timeMid;
        this.timeHiVersion = (short)timeHiVersion;
        this.clockSeq = clockSeq;
        this.node = node;
    }

    public UUID() {
        java.util.UUID randUUID = java.util.UUID.randomUUID();
        String[] tmpUUID = randUUID.toString().split("-");
        this.timeLow = (int)Long.parseLong(tmpUUID[0], 16);
        this.timeMid = (short)Integer.parseInt(tmpUUID[1], 16);
        this.timeHiVersion = (short)Integer.parseInt(tmpUUID[2], 16);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort((short)Integer.parseInt(tmpUUID[3], 16));
        this.clockSeq = buffer.array();
        buffer = ByteBuffer.allocate(8);
        buffer.putLong(Long.parseLong(tmpUUID[4], 16));
        System.arraycopy(buffer.array(), 2, this.node, 0, 6);
    }

    public void clear() {
        this.timeLow = 0;
        this.timeMid = 0;
        this.timeHiVersion = 0;
        Arrays.fill(this.clockSeq, (byte)0);
        Arrays.fill(this.node, (byte)0);
    }

    public int write(BufferWriter writer) {
        writer.writeInt4(this.timeLow);
        writer.writeInt2(this.timeMid);
        writer.writeInt2(this.timeHiVersion);
        writer.writeBytes(this.clockSeq, 2);
        writer.writeBytes(this.node, 6);
        return 16;
    }

    public void read(BufferReader reader) throws NqException {
        this.timeLow = reader.readInt4();
        this.timeMid = reader.readInt2();
        this.timeHiVersion = reader.readInt2();
        reader.readBytes(this.clockSeq, 2);
        reader.readBytes(this.node, 6);
    }

    public String toString() {
        return Integer.toHexString(this.timeLow) + "-" + Integer.toHexString(this.timeMid & 0xFFFF) + "-" + Integer.toHexString(this.timeHiVersion & 0xFFFF) + "-" + Integer.toHexString(this.clockSeq[0] & 0xFF) + Integer.toHexString(this.clockSeq[1] & 0xFF) + "-" + Integer.toHexString(this.node[0] & 0xFF) + Integer.toHexString(this.node[1] & 0xFF) + Integer.toHexString(this.node[2] & 0xFF) + Integer.toHexString(this.node[3] & 0xFF) + Integer.toHexString(this.node[4] & 0xFF) + Integer.toHexString(this.node[5] & 0xFF);
    }

    public boolean equals(Object other) {
        if (null == other) {
            return false;
        }
        UUID otherUUID = (UUID)other;
        return this.timeLow == otherUUID.timeLow && this.timeMid == otherUUID.timeMid && this.timeHiVersion == otherUUID.timeHiVersion && Arrays.equals(this.clockSeq, otherUUID.clockSeq) && Arrays.equals(this.node, otherUUID.node);
    }

    public int hashCode() {
        TraceLog.get().error("hasCode(); is used where not desired", 5, 0);
        return 0;
    }
}

