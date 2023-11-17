/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

public class PublicKeySubsystemException
extends Exception {
    private static final long serialVersionUID = 2406393624902611265L;
    static final int SUCCESS = 0;
    public static final int ACCESS_DENIED = 1;
    public static final int STORAGE_EXCEEDED = 2;
    public static final int REQUEST_NOT_SUPPPORTED = 3;
    public static final int KEY_NOT_FOUND = 4;
    public static final int KEY_NOT_SUPPORTED = 5;
    public static final int GENERAL_FAILURE = 6;
    int status;

    public PublicKeySubsystemException(int status, String desc) {
        super(desc);
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }
}

