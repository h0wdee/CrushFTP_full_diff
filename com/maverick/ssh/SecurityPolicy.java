/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.IncompatibleAlgorithm;
import com.maverick.ssh.SecurityLevel;

public interface SecurityPolicy {
    public SecurityLevel getMinimumSecurityLevel();

    public boolean isDropSecurityAsLastResort();

    public void onIncompatibleSecurity(String var1, int var2, String var3, IncompatibleAlgorithm ... var4);

    public boolean isManagedSecurity();
}

