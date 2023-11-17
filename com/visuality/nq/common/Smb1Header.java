/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import java.util.Arrays;

public class Smb1Header {
    public static final int HEADER_SIZE = 32;
    public static final int PID_OFFSET = 26;
    public int command;
    public int status;
    public int flags;
    public int flags2;
    public int pid;
    public byte[] signature;
    public int tid;
    public int uid;
    public int mid;

    public String toString() {
        return "Smb1Header [command=" + this.command + ", status=" + this.status + ", flags=" + this.flags + ", flags2=" + this.flags2 + ", pid=" + this.pid + ", signature=" + Arrays.toString(this.signature) + ", tid=" + this.tid + ", uid=" + this.uid + ", mid=" + this.mid + "]";
    }
}

