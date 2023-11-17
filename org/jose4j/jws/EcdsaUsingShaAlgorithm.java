/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jws;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jws.BaseSignatureAlgorithm;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public class EcdsaUsingShaAlgorithm
extends BaseSignatureAlgorithm
implements JsonWebSignatureAlgorithm {
    private String curveName;
    private int signatureByteLength;

    public EcdsaUsingShaAlgorithm(String id, String javaAlgo, String curveName, int signatureByteLength) {
        super(id, javaAlgo, "EC");
        this.curveName = curveName;
        this.signatureByteLength = signatureByteLength;
    }

    @Override
    public boolean verifySignature(byte[] signatureBytes, Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        byte[] derEncodedSignatureBytes;
        try {
            derEncodedSignatureBytes = EcdsaUsingShaAlgorithm.convertConcatenatedToDer(signatureBytes);
        }
        catch (IOException e) {
            throw new JoseException("Unable to convert R and S as a concatenated byte array to DER encoding.", e);
        }
        return super.verifySignature(derEncodedSignatureBytes, key, securedInputBytes, providerContext);
    }

    @Override
    public byte[] sign(Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        byte[] derEncodedSignatureBytes = super.sign(key, securedInputBytes, providerContext);
        try {
            return EcdsaUsingShaAlgorithm.convertDerToConcatenated(derEncodedSignatureBytes, this.signatureByteLength);
        }
        catch (IOException e) {
            throw new JoseException("Unable to convert DER encoding to R and S as a concatenated byte array.", e);
        }
    }

    public static byte[] convertConcatenatedToDer(byte[] concatenatedSignatureBytes) throws IOException {
        int offset;
        byte[] derEncodedSignatureBytes;
        int len;
        int rawLen;
        int i = rawLen = concatenatedSignatureBytes.length / 2;
        while (i > 0 && concatenatedSignatureBytes[rawLen - i] == 0) {
            --i;
        }
        int j = i;
        if (concatenatedSignatureBytes[rawLen - i] < 0) {
            ++j;
        }
        int k = rawLen;
        while (k > 0 && concatenatedSignatureBytes[2 * rawLen - k] == 0) {
            --k;
        }
        int l = k;
        if (concatenatedSignatureBytes[2 * rawLen - k] < 0) {
            ++l;
        }
        if ((len = 2 + j + 2 + l) > 255) {
            throw new IOException("Invalid format of ECDSA signature");
        }
        if (len < 128) {
            derEncodedSignatureBytes = new byte[4 + j + 2 + l];
            offset = 1;
        } else {
            derEncodedSignatureBytes = new byte[5 + j + 2 + l];
            derEncodedSignatureBytes[1] = -127;
            offset = 2;
        }
        derEncodedSignatureBytes[0] = 48;
        derEncodedSignatureBytes[offset++] = (byte)len;
        derEncodedSignatureBytes[offset++] = 2;
        derEncodedSignatureBytes[offset++] = (byte)j;
        System.arraycopy(concatenatedSignatureBytes, rawLen - i, derEncodedSignatureBytes, offset + j - i, i);
        offset += j;
        derEncodedSignatureBytes[offset++] = 2;
        derEncodedSignatureBytes[offset++] = (byte)l;
        System.arraycopy(concatenatedSignatureBytes, 2 * rawLen - k, derEncodedSignatureBytes, offset + l - k, k);
        return derEncodedSignatureBytes;
    }

    public static byte[] convertDerToConcatenated(byte[] derEncodedBytes, int outputLength) throws IOException {
        int sLength;
        int rLength;
        int offset;
        if (derEncodedBytes.length < 8 || derEncodedBytes[0] != 48) {
            throw new IOException("Invalid format of ECDSA signature");
        }
        if (derEncodedBytes[1] > 0) {
            offset = 2;
        } else if (derEncodedBytes[1] == -127) {
            offset = 3;
        } else {
            throw new IOException("Invalid format of ECDSA signature");
        }
        int i = rLength = derEncodedBytes[offset + 1];
        while (i > 0 && derEncodedBytes[offset + 2 + rLength - i] == 0) {
            --i;
        }
        int j = sLength = derEncodedBytes[offset + 2 + rLength + 1];
        while (j > 0 && derEncodedBytes[offset + 2 + rLength + 2 + sLength - j] == 0) {
            --j;
        }
        int rawLen = Math.max(i, j);
        rawLen = Math.max(rawLen, outputLength / 2);
        if ((derEncodedBytes[offset - 1] & 0xFF) != derEncodedBytes.length - offset || (derEncodedBytes[offset - 1] & 0xFF) != 2 + rLength + 2 + sLength || derEncodedBytes[offset] != 2 || derEncodedBytes[offset + 2 + rLength] != 2) {
            throw new IOException("Invalid format of ECDSA signature");
        }
        byte[] concatenatedSignatureBytes = new byte[2 * rawLen];
        System.arraycopy(derEncodedBytes, offset + 2 + rLength - i, concatenatedSignatureBytes, rawLen - i, i);
        System.arraycopy(derEncodedBytes, offset + 2 + rLength + 2 + sLength - j, concatenatedSignatureBytes, 2 * rawLen - j, j);
        return concatenatedSignatureBytes;
    }

    @Override
    public void validatePrivateKey(PrivateKey privateKey) throws InvalidKeyException {
        this.validateKeySpec(privateKey);
    }

    @Override
    public void validatePublicKey(PublicKey publicKey) throws InvalidKeyException {
        this.validateKeySpec(publicKey);
    }

    private void validateKeySpec(Key key) throws InvalidKeyException {
        if (key instanceof ECKey) {
            ECKey ecKey = (ECKey)((Object)key);
            ECParameterSpec spec = ecKey.getParams();
            EllipticCurve curve = spec.getCurve();
            String name = EllipticCurves.getName(curve);
            if (!this.getCurveName().equals(name)) {
                throw new InvalidKeyException(String.valueOf(this.getAlgorithmIdentifier()) + "/" + this.getJavaAlgorithm() + " expects a key using " + this.getCurveName() + " but was " + name);
            }
        }
    }

    public String getCurveName() {
        return this.curveName;
    }

    public static class EcdsaP256UsingSha256
    extends EcdsaUsingShaAlgorithm {
        public EcdsaP256UsingSha256() {
            super("ES256", "SHA256withECDSA", "P-256", 64);
        }
    }

    public static class EcdsaP384UsingSha384
    extends EcdsaUsingShaAlgorithm {
        public EcdsaP384UsingSha384() {
            super("ES384", "SHA384withECDSA", "P-384", 96);
        }
    }

    public static class EcdsaP521UsingSha512
    extends EcdsaUsingShaAlgorithm {
        public EcdsaP521UsingSha512() {
            super("ES512", "SHA512withECDSA", "P-521", 132);
        }
    }
}

