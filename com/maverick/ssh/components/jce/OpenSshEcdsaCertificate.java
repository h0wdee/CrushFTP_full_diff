/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.jce.ECUtils;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.OpenSshCertificate;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;
import com.maverick.util.ByteArrayReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSshEcdsaCertificate
extends OpenSshCertificate
implements SshPublicKey {
    static Logger log = LoggerFactory.getLogger(OpenSshEcdsaCertificate.class);
    byte[] nonce;
    String name;
    String spec;
    String curve;

    OpenSshEcdsaCertificate(String name, String spec, String curve) {
        this.name = name;
        this.spec = spec;
        this.curve = curve;
    }

    public OpenSshEcdsaCertificate(String name, ECPublicKey pub, String curve) throws IOException {
        this.name = name;
        this.publicKey = new Ssh2EcdsaSha2NistPublicKey(pub, curve);
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.STRONG;
    }

    @Override
    public int getPriority() {
        return SecurityLevel.STRONG.ordinal() * 1000 + 10;
    }

    @Override
    protected void decodePublicKey(ByteArrayReader reader) throws IOException, SshException {
        try {
            String ignored = reader.readString();
            byte[] Q = reader.readBinaryString();
            ECParameterSpec ecspec = this.getCurveParams(this.curve);
            ECPoint p = ECUtils.fromByteArray(Q, ecspec.getCurve());
            KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
            this.publicKey = new Ssh2EcdsaSha2NistPublicKey((ECPublicKey)keyFactory.generatePublic(new ECPublicKeySpec(p, ecspec)), this.curve);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new SshException("Failed to obtain ECDSA public key instance", 5, ex);
        }
    }

    @Override
    public String getAlgorithm() {
        return this.name;
    }

    @Override
    public int getBitLength() {
        return this.publicKey.getBitLength();
    }

    public byte[] getPublicOctet() {
        return ((Ssh2EcdsaSha2NistPublicKey)this.publicKey).getPublicOctet();
    }

    @Override
    public boolean verifySignature(byte[] signature, byte[] data) throws SshException {
        return this.publicKey.verifySignature(signature, data);
    }

    public ECParameterSpec getCurveParams(String curve) {
        try {
            KeyPairGenerator gen = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyPairGenerator.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyPairGenerator.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
            gen.initialize(new ECGenParameterSpec(curve), JCEProvider.getSecureRandom());
            KeyPair tmp = gen.generateKeyPair();
            return ((ECPublicKey)tmp.getPublic()).getParams();
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    @Override
    public PublicKey getJCEPublicKey() {
        return this.publicKey.getJCEPublicKey();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.publicKey == null ? 0 : this.publicKey.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        OpenSshEcdsaCertificate other = (OpenSshEcdsaCertificate)obj;
        return !(this.publicKey == null ? other.publicKey != null : !this.publicKey.equals(other.publicKey));
    }

    @Override
    public String test() {
        try {
            KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
            Signature sig = JCEProvider.getProviderForAlgorithm(this.spec) == null ? Signature.getInstance(this.spec) : Signature.getInstance(this.spec, JCEProvider.getProviderForAlgorithm(this.spec));
            return keyFactory.getProvider().getName();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}

