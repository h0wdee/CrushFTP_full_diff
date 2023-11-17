/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.types.string;

import com.hierynomus.asn1.encodingrules.ber.BERDecoder;
import com.hierynomus.asn1.types.ASN1Constructed;
import com.hierynomus.asn1.types.ASN1Encoding;
import com.hierynomus.asn1.types.ASN1Object;
import com.hierynomus.asn1.types.ASN1Primitive;
import com.hierynomus.asn1.types.ASN1Tag;
import java.util.Collections;
import java.util.Iterator;

public abstract class ASN1String<T>
extends ASN1Object<T>
implements ASN1Primitive,
ASN1Constructed {
    protected byte[] valueBytes;

    public ASN1String(ASN1Tag<?> tag, byte[] bytes) {
        super(tag);
        this.valueBytes = bytes;
    }

    @Override
    public Iterator<ASN1Object> iterator() {
        if (this.tag.getAsn1Encoding() == ASN1Encoding.Constructed) {
            return ASN1Tag.SEQUENCE.newParser(new BERDecoder()).parse(ASN1Tag.SEQUENCE, this.valueBytes).iterator();
        }
        return Collections.singletonList(this).iterator();
    }

    public abstract int length();
}

