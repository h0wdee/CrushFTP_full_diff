/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Digest
extends AbstractDigest {
    public SHA1Digest() throws NoSuchAlgorithmException {
        super("SHA-1", SecurityLevel.WEAK, 100);
    }
}

