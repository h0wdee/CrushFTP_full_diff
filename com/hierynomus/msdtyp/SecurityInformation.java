/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdtyp;

import com.hierynomus.protocol.commons.EnumWithValue;

public enum SecurityInformation implements EnumWithValue<SecurityInformation>
{
    OWNER_SECURITY_INFORMATION(1L),
    GROUP_SECURITY_INFORMATION(2L),
    DACL_SECURITY_INFORMATION(4L),
    SACL_SECURITY_INFORMATION(8L),
    LABEL_SECURITY_INFORMATION(16L),
    UNPROTECTED_SACL_SECURITY_INFORMATION(0x10000000L),
    UNPROTECTED_DACL_SECURITY_INFORMATION(0x20000000L),
    PROTECTED_SACL_SECURITY_INFORMATION(0x40000000L),
    PROTECTED_DACL_SECURITY_INFORMATION(0x80000000L),
    ATTRIBUTE_SECURITY_INFORMATION(32L),
    SCOPE_SECURITY_INFORMATION(64L),
    BACKUP_SECURITY_INFORMATION(65536L);

    private long value;

    private SecurityInformation(long value) {
        this.value = value;
    }

    @Override
    public long getValue() {
        return this.value;
    }
}

