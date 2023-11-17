/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdtyp;

import com.hierynomus.protocol.commons.EnumWithValue;

public enum AccessMask implements EnumWithValue<AccessMask>
{
    FILE_READ_DATA(1L),
    FILE_WRITE_DATA(2L),
    FILE_APPEND_DATA(4L),
    FILE_EXECUTE(32L),
    FILE_LIST_DIRECTORY(1L),
    FILE_ADD_FILE(2L),
    FILE_ADD_SUBDIRECTORY(4L),
    FILE_TRAVERSE(32L),
    FILE_DELETE_CHILD(64L),
    FILE_READ_ATTRIBUTES(128L),
    FILE_WRITE_ATTRIBUTES(256L),
    FILE_READ_EA(8L),
    FILE_WRITE_EA(16L),
    DELETE(65536L),
    READ_CONTROL(131072L),
    WRITE_DAC(262144L),
    WRITE_OWNER(524288L),
    SYNCHRONIZE(0x100000L),
    ACCESS_SYSTEM_SECURITY(0x1000000L),
    MAXIMUM_ALLOWED(0x2000000L),
    GENERIC_ALL(0x10000000L),
    GENERIC_EXECUTE(0x20000000L),
    GENERIC_WRITE(0x40000000L),
    GENERIC_READ(0x80000000L),
    ADS_RIGHT_DS_CONTROL_ACCESS(256L),
    ADS_RIGHT_DS_CREATE_CHILD(1L),
    ADS_RIGHT_DS_DELETE_CHILD(2L),
    ADS_RIGHT_DS_READ_PROP(16L),
    ADS_RIGHT_DS_WRITE_PROP(32L),
    ADS_RIGHT_DS_SELF(8L);

    private long value;

    private AccessMask(long value) {
        this.value = value;
    }

    @Override
    public long getValue() {
        return this.value;
    }
}

