/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AES128Gcm
extends AbstractJCECipher {
    static Logger log = LoggerFactory.getLogger(AES128Gcm.class);
    byte[] key;
    byte[] nonce;
    int mode;

    public AES128Gcm() throws IOException {
        super("AES/GCM/NoPadding", "AES", 16, "aes128-gcm@openssh.com", SecurityLevel.PARANOID);
    }

    @Override
    public void init(int mode, byte[] iv, byte[] keydata) throws IOException {
        this.mode = mode;
        try {
            this.key = new byte[this.keylength];
            System.arraycopy(keydata, 0, this.key, 0, this.key.length);
            SecretKeySpec kspec = new SecretKeySpec(this.key, this.keyspec);
            this.nonce = new byte[12];
            System.arraycopy(iv, 0, this.nonce, 0, this.nonce.length);
            GCMParameterSpec spec = this.cipher.getProvider().getName().equals("JsafeJCE") ? new GCMParameterSpec(16, this.nonce) : new GCMParameterSpec(128, this.nonce);
            this.cipher.init(mode == 0 ? 1 : 2, (Key)kspec, spec);
        }
        catch (InvalidKeyException e) {
            log.error("Error in JCE component", (Throwable)e);
            throw new IOException("Invalid encryption key", e);
        }
        catch (InvalidAlgorithmParameterException e) {
            log.error("Error in JCE component", (Throwable)e);
            throw new IOException("Invalid algorithm parameter", e);
        }
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
                this.cipher = this.createCipher("AES/GCM/NoPadding");
                SecretKeySpec kspec = new SecretKeySpec(this.key, this.keyspec);
                GCMParameterSpec spec = this.cipher.getProvider().getName().equals("JsafeJCE") ? new GCMParameterSpec(16, this.nonce) : new GCMParameterSpec(128, this.nonce);
                this.cipher.init(this.mode == 0 ? 1 : 2, (Key)kspec, spec);
                this.cipher.updateAAD(buf, start, 4);
                System.arraycopy(buf, start, output, off, 4);
                byte[] tmp = this.cipher.doFinal(buf, start + 4, len - 4);
                System.arraycopy(tmp, 0, output, off + 4, tmp.length);
                this.incrementIv();
            }
            catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    private void incrementIv() {
        for (int i = 11; i >= 4; --i) {
            int n = i;
            this.nonce[n] = (byte)(this.nonce[n] + 1);
            if (this.nonce[i] != 0) break;
        }
    }

    @Override
    public boolean isMAC() {
        return true;
    }

    @Override
    public int getMacLength() {
        return 16;
    }
}

