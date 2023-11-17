/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.protocol.commons.EnumWithValue;

public enum SMB2ImpersonationLevel implements EnumWithValue<SMB2ImpersonationLevel>
{
    Anonymous(0L),
    Identification(1L),
    Impersonation(2L),
    Delegate(3L);

    private long value;

    private SMB2ImpersonationLevel(long value) {
        this.value = value;
    }

    @Override
    public long getValue() {
        return this.value;
    }
}

