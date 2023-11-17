/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

public class ACL {
    public static final int ACL_ALLOWED_TYPE = 0;
    public static final int ACL_DENIED_TYPE = 1;
    public static final int ACL_AUDIT_TYPE = 2;
    public static final int ACL_ALARM_TYPE = 3;
    public static final int ACE4_FILE_INHERIT_ACE = 1;
    public static final int ACE4_DIRECTORY_INHERIT_ACE = 2;
    public static final int ACE4_NO_PROPAGATE_INHERIT_ACE = 4;
    public static final int ACE4_INHERIT_ONLY_ACE = 8;
    public static final int ACE4_SUCCESSFUL_ACCESS_ACE_FLAG = 16;
    public static final int ACE4_FAILED_ACCESS_ACE_FLAG = 32;
    public static final int ACE4_IDENTIFIER_GROUP = 64;
    public static final int ACE4_READ_DATA = 1;
    public static final int ACE4_LIST_DIRECTORY = 1;
    public static final int ACE4_WRITE_DATA = 2;
    public static final int ACE4_ADD_FILE = 2;
    public static final int ACE4_APPEND_DATA = 4;
    public static final int ACE4_ADD_SUBDIRECTORY = 4;
    public static final int ACE4_READ_NAMED_ATTRS = 8;
    public static final int ACE4_WRITE_NAMED_ATTRS = 16;
    public static final int ACE4_EXECUTE = 32;
    public static final int ACE4_DELETE_CHILD = 64;
    public static final int ACE4_READ_ATTRIBUTES = 128;
    public static final int ACE4_WRITE_ATTRIBUTES = 256;
    public static final int ACE4_DELETE = 65536;
    public static final int ACE4_READ_ACL = 131072;
    public static final int ACE4_WRITE_ACL = 262144;
    public static final int ACE4_WRITE_OWNER = 524288;
    public static final int ACE4_SYNCHRONIZE = 0x100000;
    int type;
    int flags;
    int mask;
    String who;

    public ACL(int type, int flags, int mask, String who) {
    }

    public int getType() {
        return this.type;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getMask() {
        return this.mask;
    }

    public String getWho() {
        return this.who;
    }
}

