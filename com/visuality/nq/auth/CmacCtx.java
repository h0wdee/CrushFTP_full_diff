/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

class CmacCtx {
    int numOfRounds;
    byte[] mainKey = new byte[16];
    byte[] X = new byte[16];
    byte[] M_Last = new byte[16];
    byte[] extra = new byte[16];
    byte[] key1 = new byte[16];
    byte[] key2 = new byte[16];
    int leftover;
    int flag;

    CmacCtx() {
    }
}

