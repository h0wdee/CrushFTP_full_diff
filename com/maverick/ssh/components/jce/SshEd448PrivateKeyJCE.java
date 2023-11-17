/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.SshEd25519PrivateKey;
import com.maverick.util.Arrays;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class SshEd448PrivateKeyJCE
implements SshEd25519PrivateKey {
    public static final byte[] ASN_HEADER = new byte[]{48, 71, 2, 1, 0, 48, 5, 6, 3, 43, 101, 113, 4, 59, 4, 57};
    PrivateKey key;

    public SshEd448PrivateKeyJCE(byte[] sk) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, NoSuchProviderException {
        this.loadPrivateKey(sk);
    }

    private void loadPrivateKey(byte[] sk) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyFactory keyFactory = KeyFactory.getInstance("Ed448");
        byte[] seed = Arrays.copy(sk, 57);
        byte[] encoded = Arrays.cat(ASN_HEADER, seed);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(encoded);
        this.key = keyFactory.generatePrivate(pkcs8KeySpec);
    }

    public SshEd448PrivateKeyJCE(PrivateKey prv) {
        this.key = prv;
    }

    @Override
    public byte[] sign(byte[] data) throws IOException {
        return this.sign(data, this.getAlgorithm());
    }

    @Override
    public byte[] sign(byte[] data, String signingAlgorithm) throws IOException {
        try {
            Signature sgr = Signature.getInstance("Ed448");
            sgr.initSign(this.key);
            sgr.update(data);
            return sgr.sign();
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public String getAlgorithm() {
        return "ssh-ed448";
    }

    @Override
    public PrivateKey getJCEPrivateKey() {
        return this.key;
    }

    @Override
    public byte[] getSeed() {
        byte[] encoded = this.key.getEncoded();
        byte[] seed = Arrays.copy(encoded, ASN_HEADER.length, 57);
        return seed;
    }

    public int hashCode() {
        return new String(Utils.bytesToHex(this.getSeed())).hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SshEd448PrivateKeyJCE)) {
            return false;
        }
        return Arrays.compare(this.getSeed(), ((SshEd448PrivateKeyJCE)obj).getSeed());
    }
}

