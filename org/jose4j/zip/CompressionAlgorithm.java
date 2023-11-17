/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.zip;

import org.jose4j.jwa.Algorithm;
import org.jose4j.lang.JoseException;

public interface CompressionAlgorithm
extends Algorithm {
    public byte[] compress(byte[] var1);

    public byte[] decompress(byte[] var1) throws JoseException;
}

