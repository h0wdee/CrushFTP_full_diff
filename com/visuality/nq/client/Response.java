/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.Smb2Header;

class Response {
    public byte[] buffer;
    BufferReader reader;
    Smb2Header header;
    int dataCount;
    int tailLen;
    boolean wasReceived;

    Response() {
    }
}

