/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.types;

import com.hierynomus.asn1.ASN1ParseException;
import com.hierynomus.asn1.ASN1Parser;
import com.hierynomus.asn1.ASN1Serializer;
import com.hierynomus.asn1.encodingrules.ASN1Decoder;
import com.hierynomus.asn1.encodingrules.ASN1Encoder;
import com.hierynomus.asn1.types.ASN1Encoding;
import com.hierynomus.asn1.types.ASN1Object;
import com.hierynomus.asn1.types.ASN1TagClass;
import com.hierynomus.asn1.types.constructed.ASN1Sequence;
import com.hierynomus.asn1.types.constructed.ASN1Set;
import com.hierynomus.asn1.types.constructed.ASN1TaggedObject;
import com.hierynomus.asn1.types.primitive.ASN1Boolean;
import com.hierynomus.asn1.types.primitive.ASN1Enumerated;
import com.hierynomus.asn1.types.primitive.ASN1Integer;
import com.hierynomus.asn1.types.primitive.ASN1Null;
import com.hierynomus.asn1.types.primitive.ASN1ObjectIdentifier;
import com.hierynomus.asn1.types.string.ASN1BitString;
import com.hierynomus.asn1.types.string.ASN1OctetString;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ASN1Tag<T extends ASN1Object> {
    private static Map<Integer, ASN1Tag<?>> tags = new HashMap();
    public static final ASN1Tag<ASN1Boolean> BOOLEAN = new ASN1Tag<ASN1Boolean>(ASN1TagClass.Universal, 1, ASN1Encoding.Primitive){

        @Override
        public ASN1Parser<ASN1Boolean> newParser(ASN1Decoder decoder) {
            return new ASN1Boolean.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1Boolean> newSerializer(ASN1Encoder encoder) {
            return new ASN1Boolean.Serializer(encoder);
        }
    };
    public static final ASN1Tag<ASN1Integer> INTEGER = new ASN1Tag<ASN1Integer>(ASN1TagClass.Universal, 2, ASN1Encoding.Primitive){

        @Override
        public ASN1Parser<ASN1Integer> newParser(ASN1Decoder decoder) {
            return new ASN1Integer.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1Integer> newSerializer(ASN1Encoder encoder) {
            return new ASN1Integer.Serializer(encoder);
        }
    };
    public static final ASN1Tag<ASN1BitString> BIT_STRING = new ASN1Tag<ASN1BitString>(ASN1TagClass.Universal, 3, ASN1Encoding.Primitive, EnumSet.of(ASN1Encoding.Primitive, ASN1Encoding.Constructed)){

        @Override
        public ASN1Parser<ASN1BitString> newParser(ASN1Decoder decoder) {
            return new ASN1BitString.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1BitString> newSerializer(ASN1Encoder encoder) {
            return new ASN1BitString.Serializer(encoder);
        }
    };
    public static final ASN1Tag<?> OCTET_STRING = new ASN1Tag(ASN1TagClass.Universal, 4, (EnumSet)EnumSet.of(ASN1Encoding.Primitive, ASN1Encoding.Constructed)){

        public ASN1Parser<?> newParser(ASN1Decoder decoder) {
            return new ASN1OctetString.Parser(decoder);
        }

        public ASN1Serializer newSerializer(ASN1Encoder encoder) {
            return new ASN1OctetString.Serializer(encoder);
        }
    };
    public static final ASN1Tag<ASN1Null> NULL = new ASN1Tag<ASN1Null>(ASN1TagClass.Universal, 5, ASN1Encoding.Primitive){

        @Override
        public ASN1Parser<ASN1Null> newParser(ASN1Decoder decoder) {
            return new ASN1Null.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1Null> newSerializer(ASN1Encoder encoder) {
            return new ASN1Null.Serializer(encoder);
        }
    };
    public static final ASN1Tag<ASN1ObjectIdentifier> OBJECT_IDENTIFIER = new ASN1Tag<ASN1ObjectIdentifier>(ASN1TagClass.Universal, 6, ASN1Encoding.Primitive){

        @Override
        public ASN1Parser<ASN1ObjectIdentifier> newParser(ASN1Decoder decoder) {
            return new ASN1ObjectIdentifier.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1ObjectIdentifier> newSerializer(ASN1Encoder encoder) {
            return new ASN1ObjectIdentifier.Serializer(encoder);
        }
    };
    public static final ASN1Tag<ASN1Enumerated> ENUMERATED = new ASN1Tag<ASN1Enumerated>(ASN1TagClass.Universal, 10, ASN1Encoding.Primitive){

        @Override
        public ASN1Parser<ASN1Enumerated> newParser(ASN1Decoder decoder) {
            return new ASN1Enumerated.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1Enumerated> newSerializer(ASN1Encoder encoder) {
            return new ASN1Enumerated.Serializer(encoder);
        }
    };
    public static final ASN1Tag<ASN1Set> SET = new ASN1Tag<ASN1Set>(ASN1TagClass.Universal, 17, ASN1Encoding.Constructed){

        @Override
        public ASN1Parser<ASN1Set> newParser(ASN1Decoder decoder) {
            return new ASN1Set.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1Set> newSerializer(ASN1Encoder encoder) {
            return new ASN1Set.Serializer(encoder);
        }
    };
    public static final ASN1Tag<ASN1Sequence> SEQUENCE = new ASN1Tag<ASN1Sequence>(ASN1TagClass.Universal, 16, ASN1Encoding.Constructed){

        @Override
        public ASN1Parser<ASN1Sequence> newParser(ASN1Decoder decoder) {
            return new ASN1Sequence.Parser(decoder);
        }

        @Override
        public ASN1Serializer<ASN1Sequence> newSerializer(ASN1Encoder encoder) {
            return new ASN1Sequence.Serializer(encoder);
        }
    };
    private final ASN1TagClass asn1TagClass;
    private final int tag;
    private final EnumSet<ASN1Encoding> supportedEncodings;
    private final ASN1Encoding asn1Encoding;

    static {
        tags.put(BOOLEAN.getTag(), BOOLEAN);
        tags.put(INTEGER.getTag(), INTEGER);
        tags.put(BIT_STRING.getTag(), BIT_STRING);
        tags.put(OCTET_STRING.getTag(), OCTET_STRING);
        tags.put(NULL.getTag(), NULL);
        tags.put(OBJECT_IDENTIFIER.getTag(), OBJECT_IDENTIFIER);
        tags.put(ENUMERATED.getTag(), ENUMERATED);
        tags.put(SET.getTag(), SET);
        tags.put(SEQUENCE.getTag(), SEQUENCE);
    }

    public ASN1Tag(ASN1TagClass asn1TagClass, int tag, EnumSet<ASN1Encoding> supportedEncodings) {
        this(asn1TagClass, tag, supportedEncodings.contains((Object)ASN1Encoding.Primitive) ? ASN1Encoding.Primitive : ASN1Encoding.Constructed, supportedEncodings);
    }

    public ASN1Tag(ASN1TagClass asn1TagClass, int tag, ASN1Encoding asn1Encoding) {
        this(asn1TagClass, tag, asn1Encoding, EnumSet.of(asn1Encoding));
    }

    private ASN1Tag(ASN1TagClass asn1TagClass, int tag, ASN1Encoding asn1Encoding, EnumSet<ASN1Encoding> supportedEncodings) {
        this.asn1TagClass = asn1TagClass;
        this.tag = tag;
        this.supportedEncodings = supportedEncodings;
        this.asn1Encoding = asn1Encoding;
    }

    public ASN1Tag<T> constructed() {
        return this.asEncoded(ASN1Encoding.Constructed);
    }

    public ASN1Tag<T> primitive() {
        return this.asEncoded(ASN1Encoding.Primitive);
    }

    public ASN1Tag<T> asEncoded(ASN1Encoding asn1Encoding) {
        if (this.asn1Encoding == asn1Encoding) {
            return this;
        }
        if (!this.supportedEncodings.contains((Object)asn1Encoding)) {
            throw new IllegalArgumentException(String.format("The ASN.1 tag %s does not support encoding as %s", new Object[]{this, asn1Encoding}));
        }
        return new ASN1Tag<T>(this.asn1TagClass, this.tag, asn1Encoding, this.supportedEncodings){

            @Override
            public ASN1Parser<T> newParser(ASN1Decoder decoder) {
                return ASN1Tag.this.newParser(decoder);
            }

            @Override
            public ASN1Serializer<T> newSerializer(ASN1Encoder encoder) {
                return ASN1Tag.this.newSerializer(encoder);
            }
        };
    }

    public static ASN1Tag application(int tag) {
        return ASN1Tag.forTag(ASN1TagClass.Application, tag);
    }

    public static ASN1Tag contextSpecific(int tag) {
        return ASN1Tag.forTag(ASN1TagClass.ContextSpecific, tag);
    }

    public static ASN1Tag forTag(ASN1TagClass asn1TagClass, int tag) {
        switch (asn1TagClass) {
            case Universal: {
                for (ASN1Tag<?> asn1Tag : tags.values()) {
                    if (asn1Tag.tag != tag || asn1TagClass != asn1Tag.asn1TagClass) continue;
                    return asn1Tag;
                }
                break;
            }
            case Application: 
            case ContextSpecific: 
            case Private: {
                return new ASN1Tag(asn1TagClass, tag, (EnumSet)EnumSet.of(ASN1Encoding.Primitive, ASN1Encoding.Constructed)){

                    public ASN1Parser<?> newParser(ASN1Decoder decoder) {
                        return new ASN1TaggedObject.Parser(decoder);
                    }

                    public ASN1Serializer newSerializer(ASN1Encoder encoder) {
                        return new ASN1TaggedObject.Serializer(encoder);
                    }
                };
            }
        }
        throw new ASN1ParseException(String.format("Unknown ASN.1 tag '%s:%s' found (%s)", new Object[]{asn1TagClass, tag, tags}));
    }

    public int getTag() {
        return this.tag;
    }

    public ASN1TagClass getAsn1TagClass() {
        return this.asn1TagClass;
    }

    public EnumSet<ASN1Encoding> getSupportedEncodings() {
        return EnumSet.copyOf(this.supportedEncodings);
    }

    public ASN1Encoding getAsn1Encoding() {
        return this.asn1Encoding;
    }

    public boolean isConstructed() {
        return this.asn1Encoding == ASN1Encoding.Constructed;
    }

    public abstract ASN1Parser<T> newParser(ASN1Decoder var1);

    public abstract ASN1Serializer<T> newSerializer(ASN1Encoder var1);

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ASN1Tag asn1Tag = (ASN1Tag)o;
        return this.getTag() == asn1Tag.getTag() && this.asn1TagClass == asn1Tag.asn1TagClass && this.asn1Encoding == asn1Tag.asn1Encoding;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.asn1TagClass, this.getTag(), this.asn1Encoding});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ASN1Tag[");
        sb.append((Object)this.asn1TagClass);
        sb.append(",").append((Object)this.asn1Encoding);
        sb.append(",").append(this.tag);
        sb.append(']');
        return sb.toString();
    }

    /* synthetic */ ASN1Tag(ASN1TagClass aSN1TagClass, int n, ASN1Encoding aSN1Encoding, EnumSet enumSet, ASN1Tag aSN1Tag) {
        this(aSN1TagClass, n, aSN1Encoding, enumSet);
    }
}

