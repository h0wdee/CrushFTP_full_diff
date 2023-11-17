/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys;

import javax.crypto.spec.SecretKeySpec;

public class FakeHsmNonExtractableSecretKeySpec
extends SecretKeySpec {
    public FakeHsmNonExtractableSecretKeySpec(byte[] data, String algorithm) {
        super(data, algorithm);
    }

    @Override
    public byte[] getEncoded() {
        return this.nullIt() ? null : super.getEncoded();
    }

    @Override
    public String getFormat() {
        return this.nullIt() ? null : super.getFormat();
    }

    private boolean nullIt() {
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[2];
        return stackTraceElement.getClassName().startsWith("org.jose4j");
    }
}

