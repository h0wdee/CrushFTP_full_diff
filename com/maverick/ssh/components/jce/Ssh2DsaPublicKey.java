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
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.SimpleASNWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2DsaPublicKey
implements SshDsaPublicKey {
    static Logger log = LoggerFactory.getLogger(Ssh2DsaPublicKey.class);
    protected DSAPublicKey pubkey;

    public Ssh2DsaPublicKey() {
    }

    public Ssh2DsaPublicKey(DSAPublicKey pub) {
        this.pubkey = pub;
    }

    public Ssh2DsaPublicKey(BigInteger p, BigInteger q, BigInteger g, BigInteger y) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("DSA") == null ? KeyFactory.getInstance("DSA") : KeyFactory.getInstance("DSA", JCEProvider.getProviderForAlgorithm("DSA"));
        DSAPublicKeySpec publicKeySpec = new DSAPublicKeySpec(y, p, q, g);
        this.pubkey = (DSAPublicKey)keyFactory.generatePublic(publicKeySpec);
    }

    @Override
    public DSAPublicKey getJCEPublicKey() {
        return this.pubkey;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.WEAK;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getAlgorithm() {
        return "ssh-dss";
    }

    @Override
    public String getEncodingAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public int getBitLength() {
        return this.pubkey.getParams().getP().bitLength();
    }

    @Override
    public byte[] getEncoded() throws SshException {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            baw.writeString(this.getEncodingAlgorithm());
            baw.writeBigInteger(this.pubkey.getParams().getP());
            baw.writeBigInteger(this.pubkey.getParams().getQ());
            baw.writeBigInteger(this.pubkey.getParams().getG());
            baw.writeBigInteger(this.pubkey.getY());
            byte[] byArray = baw.toByteArray();
            return byArray;
        }
        catch (IOException ioe) {
            throw new SshException("Failed to encoded DSA key", 5, ioe);
        }
        finally {
            try {
                baw.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public String getFingerprint() throws SshException {
        return SshKeyFingerprint.getFingerprint(this.getEncoded());
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        ByteArrayReader bar = new ByteArrayReader(blob, start, len);
        try {
            String header = bar.readString();
            if (!header.equals(this.getAlgorithm())) {
                throw new SshException("The encoded key is not DSA", 5);
            }
            BigInteger p = bar.readBigInteger();
            BigInteger q = bar.readBigInteger();
            BigInteger g = bar.readBigInteger();
            BigInteger y = bar.readBigInteger();
            DSAPublicKeySpec dsaKey = new DSAPublicKeySpec(y, p, q, g);
            KeyFactory kf = JCEProvider.getProviderForAlgorithm("DSA") == null ? KeyFactory.getInstance("DSA") : KeyFactory.getInstance("DSA", JCEProvider.getProviderForAlgorithm("DSA"));
            this.pubkey = (DSAPublicKey)kf.generatePublic(dsaKey);
        }
        catch (Exception ex) {
            throw new SshException("Failed to obtain DSA key instance from JCE", 5, ex);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean verifySignature(byte[] signature, byte[] data) throws SshException {
        try {
            if (signature.length != 40 && signature.length != 56 && signature.length != 64) {
                try (ByteArrayReader bar = new ByteArrayReader(signature);){
                    byte[] sig = bar.readBinaryString();
                    String header = new String(sig);
                    if (!header.equals("ssh-dss")) {
                        throw new SshException("The encoded signature is not DSA", 5);
                    }
                    signature = bar.readBinaryString();
                }
            }
            int numSize = signature.length / 2;
            byte[] r = new BigInteger(1, Arrays.copyOfRange(signature, 0, numSize)).toByteArray();
            byte[] s = new BigInteger(1, Arrays.copyOfRange(signature, numSize, signature.length)).toByteArray();
            SimpleASNWriter asn = new SimpleASNWriter();
            asn.writeByte(2);
            asn.writeData(r);
            asn.writeByte(2);
            asn.writeData(s);
            SimpleASNWriter asnEncoded = new SimpleASNWriter();
            asnEncoded.writeByte(48);
            asnEncoded.writeData(asn.toByteArray());
            byte[] encoded = asnEncoded.toByteArray();
            Signature sig = JCEProvider.getProviderForAlgorithm("SHA1WithDSA") == null ? Signature.getInstance("SHA1WithDSA") : Signature.getInstance("SHA1WithDSA", JCEProvider.getProviderForAlgorithm("SHA1WithDSA"));
            sig.initVerify(this.pubkey);
            sig.update(data);
            if (log.isDebugEnabled()) {
                log.debug("Encoded Signature: " + Utils.bytesToHex(encoded));
                log.debug("R: " + Utils.bytesToHex(r) + " len=" + r.length + " numSize=" + numSize);
                log.debug("S: " + Utils.bytesToHex(s) + " len=" + s.length + " numSize=" + numSize);
            }
            return sig.verify(encoded);
        }
        catch (Exception ex) {
            throw new SshException(16, (Throwable)ex);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof SshDsaPublicKey) {
            try {
                return ((SshPublicKey)obj).getFingerprint().equals(this.getFingerprint());
            }
            catch (SshException sshException) {
                // empty catch block
            }
        }
        return false;
    }

    public int hashCode() {
        try {
            return this.getFingerprint().hashCode();
        }
        catch (SshException ex) {
            return 0;
        }
    }

    @Override
    public BigInteger getG() {
        return this.pubkey.getParams().getG();
    }

    @Override
    public BigInteger getP() {
        return this.pubkey.getParams().getP();
    }

    @Override
    public BigInteger getQ() {
        return this.pubkey.getParams().getQ();
    }

    @Override
    public BigInteger getY() {
        return this.pubkey.getY();
    }

    @Override
    public String test() {
        try {
            KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("DSA") == null ? KeyFactory.getInstance("DSA") : KeyFactory.getInstance("DSA", JCEProvider.getProviderForAlgorithm("DSA"));
            Signature sig = JCEProvider.getProviderForAlgorithm("SHA1WithDSA") == null ? Signature.getInstance("SHA1WithDSA") : Signature.getInstance("SHA1WithDSA", JCEProvider.getProviderForAlgorithm("SHA1WithDSA"));
            return keyFactory.getProvider().getName();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public String getSigningAlgorithm() {
        return "ssh-dss";
    }
}

