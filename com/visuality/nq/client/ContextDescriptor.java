/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.DH2C;
import com.visuality.nq.client.DH2Q;
import com.visuality.nq.client.DHnC;
import com.visuality.nq.client.DHnQ;
import com.visuality.nq.client.File;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import java.util.Arrays;

public abstract class ContextDescriptor {
    public static final int DHnQ = 0;
    public static final int DHnC = 1;
    public static final int DH2Q = 2;
    public static final int DH2C = 3;
    int id = 0;
    String name = null;
    int size = 0;

    public abstract int pack(BufferWriter var1, File var2) throws NqException;

    boolean process(BufferReader bufferReader, File file) throws NqException {
        byte[] name = new byte[4];
        bufferReader.skip(16);
        bufferReader.readBytes(name, name.length);
        file.durableState = Arrays.equals(name, this.name.getBytes()) ? 3 : file.durableState;
        return 3 == file.durableState;
    }

    public abstract int getLength();

    public static ContextDescriptor ctxDscCreator(int idx) {
        ContextDescriptor ctx;
        switch (idx) {
            case 0: {
                ctx = new DHnQ();
                break;
            }
            case 1: {
                ctx = new DHnC();
                break;
            }
            case 2: {
                ctx = new DH2Q();
                break;
            }
            case 3: {
                ctx = new DH2C();
                break;
            }
            default: {
                ctx = null;
            }
        }
        return ctx;
    }
}

