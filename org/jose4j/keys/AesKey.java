/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys;

import javax.crypto.spec.SecretKeySpec;
import org.jose4j.lang.ByteUtil;

public class AesKey
extends SecretKeySpec {
    public static final String ALGORITHM = "AES";

    public AesKey(byte[] bytes) {
        super(bytes, ALGORITHM);
    }

    public String toString() {
        return String.valueOf(ByteUtil.bitLength(this.getEncoded().length)) + " bit " + ALGORITHM + " key";
    }
}

