/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractDigest;
import java.security.NoSuchAlgorithmException;

public class SHA512Digest
extends AbstractDigest {
    public SHA512Digest() throws NoSuchAlgorithmException {
        super("SHA-512", SecurityLevel.PARANOID, 0);
    }
}

