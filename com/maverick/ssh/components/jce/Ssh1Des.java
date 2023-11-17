/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class Ssh1Des
extends AbstractJCECipher {
    public Ssh1Des() throws IOException {
        super("DES/CBC/NoPadding", "DES", 8, "ssh1DES", SecurityLevel.WEAK);
    }
}

