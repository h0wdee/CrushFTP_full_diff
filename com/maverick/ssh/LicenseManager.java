/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshConnector;

public class LicenseManager {
    public static final int EXPIRED = 1;
    public static final int INVALID = 2;
    public static final int OK = 4;
    public static final int NOT_LICENSED = 8;
    public static final int EXPIRED_SUBSCRIPTION = 16;

    public static void addLicense(String license) {
        SshConnector.addLicense(license);
    }
}

