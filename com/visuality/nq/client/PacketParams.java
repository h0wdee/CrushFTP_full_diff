/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.ContextDescriptor;
import com.visuality.nq.client.File;
import com.visuality.nq.common.Blob;

public class PacketParams {
    public String path;
    public File file;
    public Blob blob;
    public int tid;
    public ContextDescriptor ctx;
    public byte infoTypes;
    public byte infoClass;
    public int dataLen;
    public int addInfo;
    public int maxResLen;
    public int useCase;
    public static final int CREATE_CREATEFIlE = 1;
    public static final int CREATE_RESTOREHANDLE = 2;
    public static final int CREATE_FINDOPEN = 3;
}

