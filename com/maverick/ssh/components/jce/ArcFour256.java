/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractJCECipher;
import com.maverick.ssh.components.jce.JCEProvider;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ArcFour256
extends AbstractJCECipher {
    public ArcFour256() throws IOException {
        super("ARCFOUR", "ARCFOUR", 32, "arcfour256", SecurityLevel.WEAK);
    }

    @Override
    public void init(int mode, byte[] iv, byte[] keydata) throws IOException {
        try {
            Cipher cipher = this.cipher = JCEProvider.getProviderForAlgorithm(this.spec) == null ? Cipher.getInstance(this.spec) : Cipher.getInstance(this.spec, JCEProvider.getProviderForAlgorithm(this.spec));
            if (this.cipher == null) {
                throw new IOException("Failed to create cipher engine for " + this.spec);
            }
            byte[] actualKey = new byte[this.keylength];
            System.arraycopy(keydata, 0, actualKey, 0, actualKey.length);
            SecretKeySpec kspec = new SecretKeySpec(actualKey, this.keyspec);
            this.cipher.init(mode == 0 ? 1 : 2, kspec);
            byte[] tmp = new byte[1536];
            this.cipher.update(tmp);
        }
        catch (NoSuchPaddingException nspe) {
            throw new IOException("Padding type not supported");
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IOException("Algorithm not supported:" + this.spec);
        }
        catch (InvalidKeyException ike) {
            throw new IOException("Invalid encryption key");
        }
    }

    @Override
    public int getBlockSize() {
        return 8;
    }
}

