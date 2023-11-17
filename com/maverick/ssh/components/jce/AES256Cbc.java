/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class AES256Cbc
extends AbstractJCECipher {
    public AES256Cbc() throws IOException {
        super("AES/CBC/NoPadding", "AES", 32, "aes256-cbc", SecurityLevel.WEAK);
    }
}

