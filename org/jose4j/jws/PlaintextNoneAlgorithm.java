/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jws;

import java.security.Key;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public class PlaintextNoneAlgorithm
extends AlgorithmInfo
implements JsonWebSignatureAlgorithm {
    private static final String CANNOT_HAVE_KEY_MESSAGE = "JWS Plaintext (alg=none) must not use a key.";

    public PlaintextNoneAlgorithm() {
        this.setAlgorithmIdentifier("none");
        this.setKeyPersuasion(KeyPersuasion.NONE);
    }

    @Override
    public boolean verifySignature(byte[] signatureBytes, Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        this.validateKey(key);
        return signatureBytes.length == 0;
    }

    @Override
    public byte[] sign(Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        this.validateKey(key);
        return ByteUtil.EMPTY_BYTES;
    }

    @Override
    public void validateSigningKey(Key key) throws InvalidKeyException {
        this.validateKey(key);
    }

    @Override
    public void validateVerificationKey(Key key) throws InvalidKeyException {
        this.validateKey(key);
    }

    private void validateKey(Key key) throws InvalidKeyException {
        if (key != null) {
            throw new InvalidKeyException(CANNOT_HAVE_KEY_MESSAGE);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

