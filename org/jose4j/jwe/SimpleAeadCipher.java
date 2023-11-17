/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import org.jose4j.jwe.CipherStrengthSupport;
import org.jose4j.jwe.CipherUtil;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;

public class SimpleAeadCipher {
    public static final String GCM_TRANSFORMATION_NAME = "AES/GCM/NoPadding";
    private String algorithm;
    private int tagByteLength;

    public SimpleAeadCipher(String algorithm, int tagByteLength) {
        this.algorithm = algorithm;
        this.tagByteLength = tagByteLength;
    }

    private Cipher getInitialisedCipher(Key key, byte[] iv, int mode, String provider) throws JoseException {
        Cipher cipher = CipherUtil.getCipher(this.algorithm, provider);
        try {
            GCMParameterSpec parameterSpec = new GCMParameterSpec(ByteUtil.bitLength(this.tagByteLength), iv);
            cipher.init(mode, key, parameterSpec);
            return cipher;
        }
        catch (InvalidKeyException e) {
            throw new JoseException("Invalid key for " + this.algorithm, e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new JoseException(e.toString(), e);
        }
    }

    public CipherOutput encrypt(Key key, byte[] iv, byte[] plaintext, byte[] aad, String provider) throws JoseException {
        byte[] cipherOutput;
        Cipher cipher = this.getInitialisedCipher(key, iv, 1, provider);
        this.updateAad(cipher, aad);
        try {
            cipherOutput = cipher.doFinal(plaintext);
        }
        catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new JoseException(e.toString(), e);
        }
        CipherOutput result = new CipherOutput();
        int tagIndex = cipherOutput.length - this.tagByteLength;
        result.ciphertext = ByteUtil.subArray(cipherOutput, 0, tagIndex);
        result.tag = ByteUtil.subArray(cipherOutput, tagIndex, this.tagByteLength);
        return result;
    }

    private void updateAad(Cipher cipher, byte[] aad) {
        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }
    }

    public byte[] decrypt(Key key, byte[] iv, byte[] ciphertext, byte[] tag, byte[] aad, String provider) throws JoseException {
        Cipher cipher = this.getInitialisedCipher(key, iv, 2, provider);
        this.updateAad(cipher, aad);
        try {
            return cipher.doFinal(ByteUtil.concat(ciphertext, tag));
        }
        catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new JoseException(e.toString(), e);
        }
    }

    public boolean isAvailable(int keyByteLength, int ivByteLength, String joseAlg) {
        boolean isAvailable = false;
        if (CipherStrengthSupport.isAvailable(this.algorithm, keyByteLength)) {
            byte[] plain = new byte[]{112, 108, 97, 105, 110, 116, 101, 120, 116};
            byte[] aad = new byte[]{97, 97, 100};
            byte[] cek = new byte[keyByteLength];
            byte[] iv = new byte[ivByteLength];
            try {
                this.encrypt(new AesKey(cek), iv, plain, aad, null);
                isAvailable = true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return isAvailable;
    }

    public static class CipherOutput {
        private byte[] ciphertext;
        private byte[] tag;

        public byte[] getCiphertext() {
            return this.ciphertext;
        }

        public byte[] getTag() {
            return this.tag;
        }
    }
}

