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
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.SshEd25519PublicKey;
import com.maverick.util.Arrays;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshEd448PublicKeyJCE
implements SshEd25519PublicKey {
    public static final byte[] ASN_HEADER = new byte[]{48, 67, 48, 5, 6, 3, 43, 101, 113, 3, 58, 0};
    static Logger log = LoggerFactory.getLogger(SshEd448PublicKeyJCE.class);
    public static final String ALGORITHM_NAME = "ssh-ed448";
    PublicKey publicKey;
    byte[] pk;

    public SshEd448PublicKeyJCE() {
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.PARANOID;
    }

    @Override
    public int getPriority() {
        return 9998;
    }

    public SshEd448PublicKeyJCE(byte[] pk) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchProviderException {
        this.pk = pk;
        this.loadPublicKey(pk);
    }

    private void loadPublicKey(byte[] pk) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchProviderException {
        KeyFactory keyFactory = JCEProvider.getKeyFactory("Ed448");
        byte[] encoded = Arrays.cat(ASN_HEADER, pk);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(encoded);
        this.publicKey = keyFactory.generatePublic(x509KeySpec);
    }

    public SshEd448PublicKeyJCE(PublicKey pub) {
        this.publicKey = pub;
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        ByteArrayReader bar = new ByteArrayReader(blob, start, len);
        try {
            String name = bar.readString();
            if (!name.equals(ALGORITHM_NAME)) {
                throw new SshException("The encoded key is not ed448", 5);
            }
            byte[] pub = bar.readBinaryString();
            this.loadPublicKey(pub);
        }
        catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            log.error("Failed to initialise public key", (Throwable)e);
            throw new SshException("Failed to read encoded key data", (Throwable)e);
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
        return ALGORITHM_NAME;
    }

    @Override
    public String getEncodingAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public int getBitLength() {
        return 448;
    }

    @Override
    public byte[] getEncoded() throws SshException {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            baw.writeString(this.getAlgorithm());
            baw.writeBinaryString(this.decodeJCEKey());
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

    private byte[] decodeJCEKey() {
        byte[] encoded = this.publicKey.getEncoded();
        byte[] seed = Arrays.copy(encoded, ASN_HEADER.length, encoded.length - ASN_HEADER.length);
        return seed;
    }

    @Override
    public byte[] getA() {
        return this.decodeJCEKey();
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
        try {
            try (ByteArrayReader bar = new ByteArrayReader(signature);){
                long count = bar.readInt();
                if (count > 0L && count == (long)this.getSigningAlgorithm().length()) {
                    bar.reset();
                    byte[] sig = bar.readBinaryString();
                    String header = new String(sig);
                    signature = bar.readBinaryString();
                }
            }
            return this.verifyJCESignature(signature, data);
        }
        catch (Exception ex) {
            throw new SshException(16, (Throwable)ex);
        }
    }

    private boolean verifyJCESignature(byte[] signature, byte[] data) throws SshException {
        try {
            Signature sgr = JCEProvider.getSignature("Ed448");
            sgr.initVerify(this.publicKey);
            sgr.update(data);
            return sgr.verify(signature);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new SshException(e, 5);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof SshEd448PublicKeyJCE) {
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
    public String getSigningAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public String test() {
        try {
            KeyFactory factory = JCEProvider.getKeyFactory("Ed448");
            return factory.getProvider().getName();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public PublicKey getJCEPublicKey() {
        return this.publicKey;
    }
}

