/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.SshPrivateKey;
import com.maverick.ssh.components.jce.Ssh2BaseJCEPrivateKey;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.SimpleASNReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;

public class Ssh2EcdsaSha2NistPrivateKey
extends Ssh2BaseJCEPrivateKey
implements SshPrivateKey {
    String name;
    String spec;
    String curve;

    public Ssh2EcdsaSha2NistPrivateKey(PrivateKey prv, String curve) throws IOException {
        this(prv, curve, null);
    }

    public Ssh2EcdsaSha2NistPrivateKey(PrivateKey prv, String curve, Provider customProvider) throws IOException {
        super(prv, customProvider);
        if (curve.equals("prime256v1") || curve.equals("secp256r1") || curve.equals("nistp256")) {
            this.curve = "secp256r1";
            this.name = "ecdsa-sha2-nistp256";
            this.spec = "SHA256WithECDSA";
        } else if (curve.equals("secp384r1") || curve.equals("nistp384")) {
            this.curve = "secp384r1";
            this.name = "ecdsa-sha2-nistp384";
            this.spec = "SHA384WithECDSA";
        } else if (curve.equals("secp521r1") || curve.equals("nistp521")) {
            this.curve = "secp521r1";
            this.name = "ecdsa-sha2-nistp521";
            this.spec = "SHA512WithECDSA";
        } else {
            throw new IOException("Unsupported curve name " + curve);
        }
    }

    @Override
    public byte[] sign(byte[] data) throws IOException {
        return this.sign(data, this.getAlgorithm());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public byte[] sign(byte[] data, String signingAlgorithm) throws IOException {
        try {
            Signature sig = this.getJCESignature(this.spec);
            sig.initSign(this.prv);
            sig.update(data);
            byte[] sigRaw = sig.sign();
            try (ByteArrayWriter baw = new ByteArrayWriter();){
                SimpleASNReader asn = new SimpleASNReader(sigRaw);
                asn.getByte();
                asn.getLength();
                asn.getByte();
                byte[] r = asn.getData();
                asn.getByte();
                byte[] s = asn.getData();
                baw.writeBinaryString(r);
                baw.writeBinaryString(s);
                byte[] byArray = baw.toByteArray();
                return byArray;
            }
        }
        catch (Exception e) {
            throw new IOException("Error in " + this.name + " sign: " + e.getMessage());
        }
    }

    @Override
    public String getAlgorithm() {
        return this.name;
    }

    @Override
    public PrivateKey getJCEPrivateKey() {
        return this.prv;
    }

    public int hashCode() {
        return this.prv.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Ssh2EcdsaSha2NistPrivateKey) {
            Ssh2EcdsaSha2NistPrivateKey other = (Ssh2EcdsaSha2NistPrivateKey)obj;
            if (other.prv != null) {
                return other.prv.equals(this.prv);
            }
        }
        return false;
    }
}

