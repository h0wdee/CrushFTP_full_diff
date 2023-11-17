/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import java.util.Arrays;

public class Smb2Header {
    public byte[] protocolId = new byte[4];
    public short size;
    public short creditCharge;
    public int status;
    public short command;
    public short credits;
    public int flags;
    public int chainOffset;
    public long mid;
    public long aid;
    public int pid;
    public int tid;
    public long sid;
    public byte[] signature = new byte[16];

    public String toString() {
        return "Smb2Header [protocolId=" + Arrays.toString(this.protocolId) + ", size=" + this.size + ", creditCharge=" + this.creditCharge + ", status=" + this.status + ", command=" + this.command + ", credits=" + this.credits + ", flags=" + this.flags + ", chainOffset=" + this.chainOffset + ", mid=" + this.mid + ", aid=" + this.aid + ", pid=" + this.pid + ", tid=" + this.tid + ", sid=" + this.sid + ", signature=" + Arrays.toString(this.signature) + "]";
    }
}

