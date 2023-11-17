/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshPrivateKey;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.Ssh2BaseJCEPrivateKey;
import com.maverick.util.SimpleASNReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;

public abstract class Ssh2BaseDsaPrivateKey
extends Ssh2BaseJCEPrivateKey
implements SshPrivateKey {
    public Ssh2BaseDsaPrivateKey(PrivateKey prv) {
        super(prv);
    }

    public Ssh2BaseDsaPrivateKey(PrivateKey prv, Provider customProvider) {
        super(prv, customProvider);
    }

    @Override
    public String getAlgorithm() {
        return "ssh-dss";
    }

    @Override
    public byte[] sign(byte[] data) throws IOException {
        return this.sign(data, this.getAlgorithm());
    }

    @Override
    public byte[] sign(byte[] data, String signingAlgorithm) throws IOException {
        try {
            Signature l_sig = this.getJCESignature("SHA1WithDSA");
            l_sig.initSign(this.prv);
            l_sig.update(data);
            byte[] signature = l_sig.sign();
            SimpleASNReader asn = new SimpleASNReader(signature);
            asn.getByte();
            asn.getLength();
            asn.getByte();
            byte[] r = Utils.stripLeadingZeros(asn.getData());
            asn.getByte();
            byte[] s = Utils.stripLeadingZeros(asn.getData());
            int numSize = this.getPublicKey().getQ().bitLength() / 4 / 2;
            byte[] decoded = null;
            decoded = new byte[numSize * 2];
            if (r.length >= numSize) {
                System.arraycopy(r, r.length - numSize, decoded, 0, numSize);
            } else {
                System.arraycopy(r, 0, decoded, numSize - r.length, r.length);
            }
            if (s.length >= numSize) {
                System.arraycopy(s, s.length - numSize, decoded, numSize, numSize);
            } else {
                System.arraycopy(s, 0, decoded, numSize + (numSize - s.length), s.length);
            }
            return decoded;
        }
        catch (Exception e) {
            throw new IOException("Failed to sign data! " + e.getMessage());
        }
    }

    public abstract SshDsaPublicKey getPublicKey();

    public int hashCode() {
        return this.prv.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Ssh2BaseDsaPrivateKey) {
            Ssh2BaseDsaPrivateKey other = (Ssh2BaseDsaPrivateKey)obj;
            if (other.prv != null) {
                return other.prv.equals(this.prv);
            }
        }
        return false;
    }
}

