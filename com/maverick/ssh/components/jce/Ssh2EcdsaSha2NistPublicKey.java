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
import com.maverick.ssh.SshKeyFingerprint;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.jce.ECUtils;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.SimpleASNWriter;
import java.io.IOException;
import java.math.BigInteger;
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

public class Ssh2EcdsaSha2NistPublicKey
implements SshPublicKey {
    static Logger log = LoggerFactory.getLogger(Ssh2EcdsaSha2NistPublicKey.class);
    String name;
    String nistpCurve;
    String spec;
    String curve;
    int priority;
    protected ECPublicKey pub;

    public Ssh2EcdsaSha2NistPublicKey(ECPublicKey pub, String curve) throws IOException {
        this.pub = pub;
        if (curve.equals("prime256v1") || curve.equals("secp256r1") || curve.equals("nistp256")) {
            this.curve = "secp256r1";
            this.nistpCurve = "nistp256";
            this.name = "ecdsa-sha2-nistp256";
            this.spec = "SHA256withECDSA";
        } else if (curve.equals("secp384r1") || curve.equals("nistp384")) {
            this.curve = "secp384r1";
            this.name = "ecdsa-sha2-nistp384";
            this.nistpCurve = "nistp384";
            this.spec = "SHA384withECDSA";
        } else if (curve.equals("secp521r1") || curve.equals("nistp521")) {
            this.curve = "secp521r1";
            this.name = "ecdsa-sha2-nistp521";
            this.nistpCurve = "nistp521";
            this.spec = "SHA512withECDSA";
        } else {
            throw new IOException("Unsupported curve name " + curve);
        }
    }

    Ssh2EcdsaSha2NistPublicKey(String name, String spec, String curve, String nistpCurve) {
        this.name = name;
        this.spec = spec;
        this.curve = curve;
        this.nistpCurve = nistpCurve;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.STRONG;
    }

    @Override
    public int getPriority() {
        return SecurityLevel.STRONG.ordinal() * 1000 + this.getCurvePriority();
    }

    private int getCurvePriority() {
        switch (this.curve) {
            default: {
                return 10;
            }
            case "secp384r1": {
                return 20;
            }
            case "secp521r1": 
        }
        return 30;
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        ByteArrayReader buf = new ByteArrayReader(blob, start, len);
        try {
            String type = buf.readString();
            buf.readString();
            byte[] Q = buf.readBinaryString();
            ECParameterSpec ecspec = this.getCurveParams(this.curve);
            ECPoint p = ECUtils.fromByteArray(Q, ecspec.getCurve());
            KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
            this.pub = (ECPublicKey)keyFactory.generatePublic(new ECPublicKeySpec(p, ecspec));
        }
        catch (Throwable t) {
            log.error("Failed to decode public key blob", t);
            throw new SshException("Failed to decode public key blob", 5);
        }
        finally {
            try {
                buf.close();
            }
            catch (IOException type) {}
        }
    }

    @Override
    public String getAlgorithm() {
        return this.name;
    }

    @Override
    public String getEncodingAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public int getBitLength() {
        return this.pub.getParams().getOrder().bitLength();
    }

    @Override
    public byte[] getEncoded() throws SshException {
        ByteArrayWriter blob = new ByteArrayWriter();
        try {
            blob.writeString(this.getEncodingAlgorithm());
            blob.writeString(this.getEncodingAlgorithm().substring(this.getEncodingAlgorithm().lastIndexOf("-") + 1));
            blob.writeBinaryString(this.getPublicOctet());
            byte[] byArray = blob.toByteArray();
            return byArray;
        }
        catch (Throwable t) {
            throw new SshException("Failed to encode public key", 5);
        }
        finally {
            try {
                blob.close();
            }
            catch (IOException iOException) {}
        }
    }

    public byte[] getPublicOctet() {
        return ECUtils.toByteArray(this.pub.getW(), this.pub.getParams().getCurve());
    }

    @Override
    public String getFingerprint() throws SshException {
        return SshKeyFingerprint.getFingerprint(this.getEncoded());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean verifySignature(byte[] signature, byte[] data) throws SshException {
        ByteArrayReader bar = new ByteArrayReader(signature);
        try {
            try {
                int count = (int)bar.readInt();
                if (count == this.name.length()) {
                    byte[] sig = new byte[count];
                    bar.read(sig);
                    String header = new String(sig);
                    if (!header.equals(this.name)) {
                        throw new SshException("The encoded signature is not ECDSA", 5);
                    }
                    signature = bar.readBinaryString();
                }
            }
            finally {
                bar.close();
            }
            bar = new ByteArrayReader(signature);
            BigInteger r = bar.readBigInteger();
            BigInteger s = bar.readBigInteger();
            SimpleASNWriter asn = new SimpleASNWriter();
            asn.writeByte(2);
            asn.writeData(r.toByteArray());
            asn.writeByte(2);
            asn.writeData(s.toByteArray());
            SimpleASNWriter asnEncoded = new SimpleASNWriter();
            asnEncoded.writeByte(48);
            asnEncoded.writeData(asn.toByteArray());
            byte[] encoded = asnEncoded.toByteArray();
            Signature sig = JCEProvider.getProviderForAlgorithm(this.spec) == null ? Signature.getInstance(this.spec) : Signature.getInstance(this.spec, JCEProvider.getProviderForAlgorithm(this.spec));
            sig.initVerify(this.pub);
            sig.update(data);
            boolean bl = sig.verify(encoded);
            return bl;
        }
        catch (Exception ex) {
            throw new SshException(16, (Throwable)ex);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
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
        return this.pub;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.pub == null ? 0 : this.pub.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Ssh2EcdsaSha2NistPublicKey)) {
            return false;
        }
        Ssh2EcdsaSha2NistPublicKey other = (Ssh2EcdsaSha2NistPublicKey)obj;
        return !(this.pub == null ? other.pub != null : !this.pub.equals(other.pub));
    }

    @Override
    public String getSigningAlgorithm() {
        return this.getAlgorithm();
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

    public byte[] getOid() {
        return ECUtils.getOidBytes(this.curve);
    }

    public String getCurve() {
        return this.nistpCurve;
    }
}

