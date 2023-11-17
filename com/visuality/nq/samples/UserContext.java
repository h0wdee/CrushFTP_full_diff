/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

class UserContext {
    String activity;
    long numberOfBytes = 0L;
    long expectedNumberOfBytesToWrite = -1L;
    int errCode = 0;
    boolean started = false;

    UserContext(String activity, long expectedNumberOfBytesToWrite) {
        this.activity = activity;
        this.expectedNumberOfBytesToWrite = expectedNumberOfBytesToWrite;
    }

    UserContext(String activity) {
        this.activity = activity;
    }
}

