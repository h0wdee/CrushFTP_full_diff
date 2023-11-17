/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class AES128Cbc
extends AbstractJCECipher {
    public AES128Cbc() throws IOException {
        super("AES/CBC/NoPadding", "AES", 16, "aes128-cbc", SecurityLevel.WEAK);
    }
}

