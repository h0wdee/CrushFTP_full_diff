/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.encodingrules;

import com.hierynomus.asn1.types.ASN1Tag;
import java.io.InputStream;

public interface ASN1Decoder {
    public ASN1Tag<?> readTag(InputStream var1);

    public int readLength(InputStream var1);

    public byte[] readValue(int var1, InputStream var2);
}

