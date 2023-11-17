/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class AES192Cbc
extends AbstractJCECipher {
    public AES192Cbc() throws IOException {
        super("AES/CBC/NoPadding", "AES", 24, "aes192-cbc", SecurityLevel.WEAK);
    }
}

