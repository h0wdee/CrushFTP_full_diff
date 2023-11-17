/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecureComponent;
import com.maverick.ssh.SshException;
import java.security.PublicKey;

public interface SshPublicKey
extends SecureComponent {
    public void init(byte[] var1, int var2, int var3) throws SshException;

    @Override
    public String getAlgorithm();

    public String getSigningAlgorithm();

    public String getEncodingAlgorithm();

    public int getBitLength();

    public byte[] getEncoded() throws SshException;

    public String getFingerprint() throws SshException;

    public boolean verifySignature(byte[] var1, byte[] var2) throws SshException;

    public String test();

    public PublicKey getJCEPublicKey();
}

