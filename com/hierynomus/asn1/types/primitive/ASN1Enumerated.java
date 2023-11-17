/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.types.primitive;

import com.hierynomus.asn1.ASN1OutputStream;
import com.hierynomus.asn1.ASN1ParseException;
import com.hierynomus.asn1.ASN1Parser;
import com.hierynomus.asn1.ASN1Serializer;
import com.hierynomus.asn1.encodingrules.ASN1Decoder;
import com.hierynomus.asn1.encodingrules.ASN1Encoder;
import com.hierynomus.asn1.types.ASN1Tag;
import com.hierynomus.asn1.types.primitive.ASN1PrimitiveValue;
import java.io.IOException;
import java.math.BigInteger;

public class ASN1Enumerated
extends ASN1PrimitiveValue<BigInteger> {
    private final BigInteger value;

    public ASN1Enumerated(int value) {
        this(BigInteger.valueOf(value));
    }

    public ASN1Enumerated(BigInteger value) {
        super(ASN1Tag.ENUMERATED);
        this.value = value;
    }

    private ASN1Enumerated(BigInteger value, byte[] valueBytes) {
        super(ASN1Tag.ENUMERATED, valueBytes);
        this.value = value;
    }

    @Override
    public BigInteger getValue() {
        return this.value;
    }

    /* synthetic */ ASN1Enumerated(BigInteger bigInteger, byte[] byArray, ASN1Enumerated aSN1Enumerated) {
        this(bigInteger, byArray);
    }

    public static class Parser
    extends ASN1Parser<ASN1Enumerated> {
        public Parser(ASN1Decoder decoder) {
            super(decoder);
        }

        @Override
        public ASN1Enumerated parse(ASN1Tag<ASN1Enumerated> asn1Tag, byte[] value) throws ASN1ParseException {
            BigInteger enumValue = new BigInteger(value);
            return new ASN1Enumerated(enumValue, value, null);
        }
    }

    public static class Serializer
    extends ASN1Serializer<ASN1Enumerated> {
        public Serializer(ASN1Encoder encoder) {
            super(encoder);
        }

        @Override
        public int serializedLength(ASN1Enumerated asn1Object) {
            if (asn1Object.valueBytes == null) {
                this.calculateBytes(asn1Object);
            }
            return asn1Object.valueBytes.length;
        }

        @Override
        public void serialize(ASN1Enumerated asn1Object, ASN1OutputStream stream) throws IOException {
            if (asn1Object.valueBytes == null) {
                this.calculateBytes(asn1Object);
            }
            stream.write(asn1Object.valueBytes);
        }

        private void calculateBytes(ASN1Enumerated asn1Object) {
            asn1Object.valueBytes = asn1Object.value.toByteArray();
        }
    }
}

