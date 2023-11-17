/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;

public class BlowfishCbc
extends AbstractJCECipher {
    public BlowfishCbc() throws IOException {
        super("Blowfish/CBC/NoPadding", "Blowfish", 16, "blowfish-cbc", SecurityLevel.WEAK);
    }
}

