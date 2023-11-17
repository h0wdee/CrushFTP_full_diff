/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractDigest;
import java.security.NoSuchAlgorithmException;

public class SHA384Digest
extends AbstractDigest {
    public SHA384Digest() throws NoSuchAlgorithmException {
        super("SHA-384", SecurityLevel.PARANOID, 0);
    }
}

