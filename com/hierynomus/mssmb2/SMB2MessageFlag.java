/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.protocol.commons.EnumWithValue;

public enum SMB2MessageFlag implements EnumWithValue<SMB2MessageFlag>
{
    SMB2_FLAGS_SERVER_TO_REDIR(1L),
    SMB2_FLAGS_ASYNC_COMMAND(2L),
    SMB2_FLAGS_RELATED_OPERATIONS(4L),
    SMB2_FLAGS_SIGNED(8L),
    SMB2_FLAGS_PRIORITY_MASK(112L),
    SMB2_FLAGS_DFS_OPERATIONS(0x10000000L),
    SMB2_FLAGS_REPLAY_OPERATION(0x20000000L);

    private long value;

    private SMB2MessageFlag(long value) {
        this.value = value;
    }

    @Override
    public long getValue() {
        return this.value;
    }
}

