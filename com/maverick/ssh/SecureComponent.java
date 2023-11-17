/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SecurityLevel;

public interface SecureComponent {
    public SecurityLevel getSecurityLevel();

    public int getPriority();

    public String getAlgorithm();
}

