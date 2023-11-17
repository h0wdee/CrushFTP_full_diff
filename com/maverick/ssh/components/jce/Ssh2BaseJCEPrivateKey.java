/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.SshPrivateKey;
import com.maverick.ssh.components.jce.JCEProvider;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;

public abstract class Ssh2BaseJCEPrivateKey
implements SshPrivateKey {
    protected PrivateKey prv;
    protected Provider customProvider;

    public Ssh2BaseJCEPrivateKey(PrivateKey prv) {
        this.prv = prv;
    }

    public Ssh2BaseJCEPrivateKey(PrivateKey prv, Provider customProvider) {
        this.prv = prv;
        this.customProvider = customProvider;
    }

    @Override
    public PrivateKey getJCEPrivateKey() {
        return this.prv;
    }

    protected Signature getJCESignature(String algorithm) throws NoSuchAlgorithmException {
        Signature sig = null;
        if (this.customProvider != null) {
            try {
                sig = Signature.getInstance(algorithm, this.customProvider);
            }
            catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                // empty catch block
            }
        }
        if (sig == null) {
            sig = JCEProvider.getProviderForAlgorithm(algorithm) == null ? Signature.getInstance(algorithm) : Signature.getInstance(algorithm, JCEProvider.getProviderForAlgorithm(algorithm));
        }
        return sig;
    }
}

