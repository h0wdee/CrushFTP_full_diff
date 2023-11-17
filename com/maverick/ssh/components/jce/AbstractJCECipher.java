/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.jce.JCEProvider;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AbstractJCECipher
extends SshCipher {
    Cipher cipher;
    String spec;
    String keyspec;
    int keylength;

    public AbstractJCECipher(String spec, String keyspec, int keylength, String algorithm, SecurityLevel securityLevel) throws IOException {
        super(algorithm, securityLevel);
        this.spec = spec;
        this.keylength = keylength;
        this.keyspec = keyspec;
        try {
            this.cipher = this.createCipher(spec);
        }
        catch (NoSuchPaddingException nspe) {
            throw new IOException("Padding type not supported");
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IOException("Algorithm not supported:" + spec);
        }
        if (this.cipher == null) {
            throw new IOException("Failed to create cipher engine for " + spec);
        }
    }

    @Override
    public String getProviderName() {
        return this.cipher.getProvider().getName();
    }

    protected Cipher createCipher(String spec) throws NoSuchAlgorithmException, NoSuchPaddingException {
        return JCEProvider.getProviderForAlgorithm(spec) == null ? Cipher.getInstance(spec) : Cipher.getInstance(spec, JCEProvider.getProviderForAlgorithm(spec));
    }

    @Override
    public void transform(byte[] buf, int start, byte[] output, int off, int len) throws IOException {
        if (len > 0) {
            if (buf.length - start < len) {
                throw new IllegalStateException("Input buffer of " + buf.length + " bytes is too small for requested transform length " + len);
            }
            if (output.length - off < len) {
                throw new IllegalStateException("Output buffer of " + output.length + " bytes is too small for requested transform length " + len);
            }
            try {
                this.cipher.update(buf, start, len, output, off);
            }
            catch (ShortBufferException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    public String getProvider() {
        if (this.cipher == null) {
            return null;
        }
        return this.cipher.getProvider().getName();
    }

    @Override
    public int getKeyLength() {
        return this.keylength;
    }

    @Override
    public void init(int mode, byte[] iv, byte[] keydata) throws IOException {
        try {
            byte[] actualKey = new byte[this.keylength];
            System.arraycopy(keydata, 0, actualKey, 0, actualKey.length);
            SecretKeySpec kspec = this.generateSecretKeySpec(actualKey);
            AlgorithmParameterSpec pspec = this.generateAlgorithmSpec(iv);
            this.cipher.init(mode == 0 ? 1 : 2, (Key)kspec, pspec);
        }
        catch (InvalidKeyException ike) {
            throw new IOException("Invalid encryption key");
        }
        catch (InvalidAlgorithmParameterException ape) {
            throw new IOException("Invalid algorithm parameter");
        }
    }

    protected AlgorithmParameterSpec generateAlgorithmSpec(byte[] iv) {
        return new IvParameterSpec(iv, 0, this.getBlockSize());
    }

    protected SecretKeySpec generateSecretKeySpec(byte[] actualKey) {
        return new SecretKeySpec(actualKey, this.keyspec);
    }

    @Override
    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }
}

