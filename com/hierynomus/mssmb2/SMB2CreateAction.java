/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.protocol.commons.EnumWithValue;

public enum SMB2CreateAction implements EnumWithValue<SMB2CreateAction>
{
    FILE_SUPERSEDED(0L),
    FILE_OPENED(1L),
    FILE_CREATED(2L),
    FILE_OVERWRITTEN(3L);

    private long value;

    private SMB2CreateAction(long value) {
        this.value = value;
    }

    @Override
    public long getValue() {
        return this.value;
    }
}

