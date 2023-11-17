/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe.kdf;

import java.io.ByteArrayOutputStream;
import javax.crypto.Mac;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.jose4j.mac.MacUtil;

public class PasswordBasedKeyDerivationFunction2 {
    private String hmacAlgorithm;

    public PasswordBasedKeyDerivationFunction2(String hmacAlgorithm) {
        this.hmacAlgorithm = hmacAlgorithm;
    }

    public byte[] derive(byte[] password, byte[] salt, int iterationCount, int dkLen) throws JoseException {
        return this.derive(password, salt, iterationCount, dkLen, null);
    }

    public byte[] derive(byte[] password, byte[] salt, int iterationCount, int dkLen, String provider) throws JoseException {
        Mac prf = MacUtil.getInitializedMac(this.hmacAlgorithm, new HmacKey(password), provider);
        int hLen = prf.getMacLength();
        long maxDerivedKeyLength = 0xFFFFFFFFL;
        if ((long)dkLen > maxDerivedKeyLength) {
            throw new UncheckedJoseException("derived key too long " + dkLen);
        }
        int l = (int)Math.ceil((double)dkLen / (double)hLen);
        int r = dkLen - (l - 1) * hLen;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        while (i < l) {
            byte[] block = this.f(salt, iterationCount, i + 1, prf);
            if (i == l - 1) {
                block = ByteUtil.subArray(block, 0, r);
            }
            byteArrayOutputStream.write(block, 0, block.length);
            ++i;
        }
        return byteArrayOutputStream.toByteArray();
    }

    byte[] f(byte[] salt, int iterationCount, int blockIndex, Mac prf) {
        byte[] lastU = null;
        byte[] xorU = null;
        int i = 1;
        while (i <= iterationCount) {
            byte[] currentU;
            if (i == 1) {
                byte[] inputBytes = ByteUtil.concat(salt, ByteUtil.getBytes(blockIndex));
                xorU = currentU = prf.doFinal(inputBytes);
            } else {
                currentU = prf.doFinal(lastU);
                int j = 0;
                while (j < currentU.length) {
                    xorU[j] = (byte)(currentU[j] ^ xorU[j]);
                    ++j;
                }
            }
            lastU = currentU;
            ++i;
        }
        return xorU;
    }
}

