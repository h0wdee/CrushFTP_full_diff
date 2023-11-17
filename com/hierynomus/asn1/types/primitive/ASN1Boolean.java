/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.types.primitive;

import com.hierynomus.asn1.ASN1OutputStream;
import com.hierynomus.asn1.ASN1Parser;
import com.hierynomus.asn1.ASN1Serializer;
import com.hierynomus.asn1.encodingrules.ASN1Decoder;
import com.hierynomus.asn1.encodingrules.ASN1Encoder;
import com.hierynomus.asn1.types.ASN1Tag;
import com.hierynomus.asn1.types.primitive.ASN1PrimitiveValue;
import com.hierynomus.asn1.util.Checks;
import java.io.IOException;

public class ASN1Boolean
extends ASN1PrimitiveValue<Boolean> {
    private boolean value;

    public ASN1Boolean(boolean value) {
        super(ASN1Tag.BOOLEAN);
        this.value = value;
    }

    private ASN1Boolean(byte[] valueBytes, boolean value) {
        super(ASN1Tag.BOOLEAN, valueBytes);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    protected int valueHash() {
        return this.value ? 1231 : 1237;
    }

    /* synthetic */ ASN1Boolean(byte[] byArray, boolean bl, ASN1Boolean aSN1Boolean) {
        this(byArray, bl);
    }

    public static class Parser
    extends ASN1Parser<ASN1Boolean> {
        public Parser(ASN1Decoder decoder) {
            super(decoder);
        }

        @Override
        public ASN1Boolean parse(ASN1Tag<ASN1Boolean> asn1Tag, byte[] value) {
            Checks.checkState(value.length == 1, "Value of ASN1Boolean should have length 1, but was %s", value.length);
            return new ASN1Boolean(value, value[0] != 0, null);
        }
    }

    public static class Serializer
    extends ASN1Serializer<ASN1Boolean> {
        public Serializer(ASN1Encoder encoder) {
            super(encoder);
        }

        @Override
        public int serializedLength(ASN1Boolean asn1Object) {
            return 1;
        }

        @Override
        public void serialize(ASN1Boolean asn1Object, ASN1OutputStream stream) throws IOException {
            stream.write(asn1Object.value ? 1 : 0);
        }
    }
}

