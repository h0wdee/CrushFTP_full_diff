/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class TripleDesCbc
extends AbstractJCECipher {
    public TripleDesCbc() throws IOException {
        super("DESede/CBC/NoPadding", "DESede", 24, "3des-cbc", SecurityLevel.WEAK);
    }
}

