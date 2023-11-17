/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.Smb2Header;

class Request {
    Buffer buffer;
    BufferWriter writer;
    Smb2Header header;
    Buffer tail;
    public short command;
    long userId;
    boolean encrypt;

    Request() {
    }

    public String toString() {
        return "Request [header=" + this.header + ", command=" + this.command + ", userId=" + this.userId + ", encrypt=" + this.encrypt + "]";
    }
}

