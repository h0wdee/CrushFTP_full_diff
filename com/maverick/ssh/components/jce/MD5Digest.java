/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Digest
extends AbstractDigest {
    public MD5Digest() throws NoSuchAlgorithmException {
        super("MD5", SecurityLevel.WEAK, 0);
    }
}

