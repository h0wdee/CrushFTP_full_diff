/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class AES128Ctr
extends AbstractJCECipher {
    public AES128Ctr() throws IOException {
        super("AES/CTR/NoPadding", "AES", 16, "aes128-ctr", SecurityLevel.STRONG);
    }
}

