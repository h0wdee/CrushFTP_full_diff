/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.protocol.commons.EnumWithValue;

public enum SMB2CreateOptions implements EnumWithValue<SMB2CreateOptions>
{
    FILE_DIRECTORY_FILE(1L),
    FILE_WRITE_THROUGH(2L),
    FILE_SEQUENTIAL_ONLY(4L),
    FILE_NO_INTERMEDIATE_BUFFERING(8L),
    FILE_SYNCHRONOUS_IO_ALERT(16L),
    FILE_SYNCHRONOUS_IO_NONALERT(32L),
    FILE_NON_DIRECTORY_FILE(64L),
    FILE_COMPLETE_IF_OPLOCKED(256L),
    FILE_NO_EA_KNOWLEDGE(512L),
    FILE_RANDOM_ACCESS(2048L),
    FILE_DELETE_ON_CLOSE(4096L),
    FILE_OPEN_BY_FILE_ID(8192L),
    FILE_OPEN_FOR_BACKUP_INTENT(16384L),
    FILE_NO_COMPRESSION(32768L),
    FILE_OPEN_REMOTE_INSTANCE(1024L),
    FILE_OPEN_REQUIRING_OPLOCK(65536L),
    FILE_DISALLOW_EXCLUSIVE(131072L),
    FILE_RESERVE_OPFILTER(0x100000L),
    FILE_OPEN_REPARSE_POINT(0x200000L),
    FILE_OPEN_NO_RECALL(0x400000L),
    FILE_OPEN_FOR_FREE_SPACE_QUERY(0x800000L);

    private long value;

    private SMB2CreateOptions(long value) {
        this.value = value;
    }

    @Override
    public long getValue() {
        return this.value;
    }
}

