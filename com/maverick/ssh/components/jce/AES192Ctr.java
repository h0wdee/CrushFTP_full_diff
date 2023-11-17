/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class AES192Ctr
extends AbstractJCECipher {
    public AES192Ctr() throws IOException {
        super("AES/CTR/NoPadding", "AES", 24, "aes192-ctr", SecurityLevel.STRONG);
    }
}

