/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Digest
extends AbstractDigest {
    public SHA256Digest() throws NoSuchAlgorithmException {
        super("SHA-256", SecurityLevel.STRONG, 0);
    }
}

