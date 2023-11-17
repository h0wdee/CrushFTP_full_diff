/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class TripleDesCtr
extends AbstractJCECipher {
    public TripleDesCtr() throws IOException {
        super("DESede/CTR/NoPadding", "DESede", 24, "3des-ctr", SecurityLevel.WEAK);
    }
}

