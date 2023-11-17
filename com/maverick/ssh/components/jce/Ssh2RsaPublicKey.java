/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshKeyFingerprint;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2RsaPublicKey
implements SshRsaPublicKey {
    static Logger log = LoggerFactory.getLogger(Ssh2RsaPublicKey.class);
    RSAPublicKey pubKey;
    SecurityLevel securityLevel;
    int priority;

    public Ssh2RsaPublicKey() {
        this(SecurityLevel.WEAK, 100);
    }

    public Ssh2RsaPublicKey(RSAPublicKey pubKey) {
        this(pubKey, SecurityLevel.WEAK, 100);
    }

    public Ssh2RsaPublicKey(SecurityLevel securityLevel, int priority) {
        this.securityLevel = securityLevel;
        this.priority = priority;
    }

    public Ssh2RsaPublicKey(RSAPublicKey pubKey, SecurityLevel securityLevel, int priority) {
        this(securityLevel, priority);
        this.pubKey = pubKey;
    }

    public Ssh2RsaPublicKey(BigInteger modulus, BigInteger publicExponent) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(modulus, publicExponent, SecurityLevel.WEAK, 100);
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    @Override
    public int getPriority() {
        return this.securityLevel.ordinal() * 1000 + this.priority;
    }

    public Ssh2RsaPublicKey(BigInteger modulus, BigInteger publicExponent, SecurityLevel securityLevel, int priority) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this(securityLevel, priority);
        KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("RSA") == null ? KeyFactory.getInstance("RSA") : KeyFactory.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA"));
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
        this.pubKey = (RSAPublicKey)keyFactory.generatePublic(spec);
    }

    @Override
    public byte[] getEncoded() throws SshException {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            baw.writeString(this.getEncodingAlgorithm());
            baw.writeBigInteger(this.pubKey.getPublicExponent());
            baw.writeBigInteger(this.pubKey.getModulus());
            byte[] byArray = baw.toByteArray();
            return byArray;
        }
        catch (IOException ex) {
            throw new SshException("Failed to encoded key data", 5, ex);
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
    public int getBitLength() {
        return this.pubKey.getModulus().bitLength();
    }

    @Override
    public String getEncodingAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        ByteArrayReader bar = new ByteArrayReader(blob, start, len);
        try {
            String header = bar.readString();
            if (!header.equals(this.getEncodingAlgorithm()) && !header.equals(this.getSigningAlgorithm())) {
                throw new SshException("The encoded key " + header + " is not encoded using " + this.getEncodingAlgorithm() + " or neither its signature type " + this.getSigningAlgorithm(), 5);
            }
            BigInteger e = bar.readBigInteger();
            BigInteger n = bar.readBigInteger();
            RSAPublicKeySpec rsaKey = new RSAPublicKeySpec(n, e);
            try {
                KeyFactory kf = JCEProvider.getProviderForAlgorithm("RSA") == null ? KeyFactory.getInstance("RSA") : KeyFactory.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA"));
                this.pubKey = (RSAPublicKey)kf.generatePublic(rsaKey);
            }
            catch (Exception ex) {
                throw new SshException("Failed to obtain RSA key instance from JCE", 5, ex);
            }
        }
        catch (IOException ioe) {
            throw new SshException("Failed to read encoded key data", 5);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public String getAlgorithm() {
        return "ssh-rsa";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean verifySignature(byte[] signature, byte[] data) throws SshException {
        try {
            String signatureAlgorithm = "ssh-rsa";
            try (ByteArrayReader bar = new ByteArrayReader(signature);){
                long count = bar.readInt();
                if (count > 0L && count < 100L) {
                    bar.reset();
                    byte[] sig = bar.readBinaryString();
                    signatureAlgorithm = new String(sig);
                    signature = bar.readBinaryString();
                }
            }
            return this.verifyJCESignature(signature, signatureAlgorithm, data, true);
        }
        catch (Exception ex) {
            throw new SshException(16, (Throwable)ex);
        }
    }

    public int getSignatureLength() {
        int length = this.getModulus().bitLength() / 8;
        int mod = this.getModulus().bitLength() % 8;
        if (mod != 0) {
            ++length;
        }
        return length;
    }

    private boolean verifyJCESignature(byte[] signature, String signatureAlgorithm, byte[] data, boolean allowCorrect) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        boolean result;
        byte[] original;
        boolean corrected;
        int signatureLength;
        int expectedLength;
        block16: {
            Signature s;
            switch (signatureAlgorithm) {
                case "rsa-sha2-256": {
                    s = JCEProvider.getProviderForAlgorithm("SHA256WithRSA") == null ? Signature.getInstance("SHA256WithRSA") : Signature.getInstance("SHA256WithRSA", JCEProvider.getProviderForAlgorithm("SHA256WithRSA"));
                    break;
                }
                case "rsa-sha2-512": {
                    s = JCEProvider.getProviderForAlgorithm("SHA512WithRSA") == null ? Signature.getInstance("SHA512WithRSA") : Signature.getInstance("SHA512WithRSA", JCEProvider.getProviderForAlgorithm("SHA512WithRSA"));
                    break;
                }
                default: {
                    s = JCEProvider.getProviderForAlgorithm("SHA1WithRSA") == null ? Signature.getInstance("SHA1WithRSA") : Signature.getInstance("SHA1WithRSA", JCEProvider.getProviderForAlgorithm("SHA1WithRSA"));
                }
            }
            s.initVerify(this.pubKey);
            s.update(data);
            expectedLength = this.getSignatureLength();
            signatureLength = signature.length;
            corrected = false;
            original = signature;
            if (allowCorrect && signature.length < expectedLength) {
                if (log.isDebugEnabled()) {
                    log.debug("No Padding Detected: Expected signature length of " + expectedLength + " (modulus=" + this.getModulus().bitLength() + ") but got " + signature.length);
                }
                byte[] tmp = new byte[expectedLength];
                System.arraycopy(signature, 0, tmp, expectedLength - signature.length, signature.length);
                signature = tmp;
                corrected = true;
            }
            result = false;
            try {
                result = s.verify(signature);
            }
            catch (SignatureException e) {
                if (!allowCorrect) {
                    throw e;
                }
                if (!log.isDebugEnabled()) break block16;
                log.debug("Signature failed. Falling back to raw signature data.");
            }
        }
        if (!result) {
            if (corrected) {
                result = this.verifyJCESignature(original, signatureAlgorithm, data, false);
            }
            if (!result && log.isDebugEnabled() && AdaptiveConfiguration.getBoolean("verbose", false, new String[0])) {
                log.debug("JCE Reports Invalid Signature: Expected signature length of " + expectedLength + " (modulus=" + this.getModulus().bitLength() + ") but got " + signatureLength);
            }
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SshRsaPublicKey) {
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
    public BigInteger doPublic(BigInteger input) throws SshException {
        try {
            Cipher cipher = JCEProvider.getProviderForAlgorithm("RSA_Cipher") == null ? Cipher.getInstance("RSA") : Cipher.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA_Cipher"));
            cipher.init(1, (Key)this.pubKey, JCEProvider.getSecureRandom());
            byte[] tmp = input.toByteArray();
            return new BigInteger(cipher.doFinal(tmp, tmp[0] == 0 ? 1 : 0, tmp[0] == 0 ? tmp.length - 1 : tmp.length));
        }
        catch (Throwable e) {
            if (e.getMessage().indexOf("RSA") > -1) {
                throw new SshException("JCE provider requires BouncyCastle provider for RSA/NONE/PKCS1Padding component. Add bcprov.jar to your classpath or configure an alternative provider for this algorithm", 5);
            }
            throw new SshException(e);
        }
    }

    @Override
    public BigInteger getModulus() {
        return this.pubKey.getModulus();
    }

    @Override
    public BigInteger getPublicExponent() {
        return this.pubKey.getPublicExponent();
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public PublicKey getJCEPublicKey() {
        return this.pubKey;
    }

    @Override
    public String test() {
        try {
            KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("RSA") == null ? KeyFactory.getInstance("RSA") : KeyFactory.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA"));
            Cipher cipher = JCEProvider.getProviderForAlgorithm("RSA_Cipher") == null ? Cipher.getInstance("RSA") : Cipher.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA_Cipher"));
            Signature s = JCEProvider.getProviderForAlgorithm("SHA1WithRSA") == null ? Signature.getInstance("SHA1WithRSA") : Signature.getInstance("SHA1WithRSA", JCEProvider.getProviderForAlgorithm("SHA1WithRSA"));
            return keyFactory.getProvider().getName();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public String getSigningAlgorithm() {
        return "ssh-rsa";
    }
}

