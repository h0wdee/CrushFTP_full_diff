/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1;

import com.hierynomus.asn1.ASN1OutputStream;
import com.hierynomus.asn1.encodingrules.ASN1Encoder;
import com.hierynomus.asn1.types.ASN1Object;
import java.io.IOException;

public abstract class ASN1Serializer<T extends ASN1Object> {
    protected final ASN1Encoder encoder;

    public ASN1Serializer(ASN1Encoder encoder) {
        this.encoder = encoder;
    }

    public abstract int serializedLength(T var1) throws IOException;

    public abstract void serialize(T var1, ASN1OutputStream var2) throws IOException;
}

