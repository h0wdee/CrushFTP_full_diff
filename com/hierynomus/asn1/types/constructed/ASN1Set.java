/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.types.constructed;

import com.hierynomus.asn1.ASN1InputStream;
import com.hierynomus.asn1.ASN1OutputStream;
import com.hierynomus.asn1.ASN1ParseException;
import com.hierynomus.asn1.ASN1Parser;
import com.hierynomus.asn1.ASN1Serializer;
import com.hierynomus.asn1.encodingrules.ASN1Decoder;
import com.hierynomus.asn1.encodingrules.ASN1Encoder;
import com.hierynomus.asn1.types.ASN1Constructed;
import com.hierynomus.asn1.types.ASN1Object;
import com.hierynomus.asn1.types.ASN1Tag;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ASN1Set
extends ASN1Object<Set<ASN1Object>>
implements ASN1Constructed {
    private final Set<ASN1Object> objects;
    private byte[] bytes;

    private ASN1Set(Set<ASN1Object> objects, byte[] bytes) {
        super(ASN1Tag.SET);
        this.objects = objects;
        this.bytes = bytes;
    }

    public ASN1Set(Set<ASN1Object> objects) {
        super(ASN1Tag.SET);
        this.objects = new HashSet<ASN1Object>(objects);
    }

    @Override
    public Set<ASN1Object> getValue() {
        return new HashSet<ASN1Object>(this.objects);
    }

    @Override
    public Iterator<ASN1Object> iterator() {
        return new HashSet<ASN1Object>(this.objects).iterator();
    }

    /* synthetic */ ASN1Set(Set set, byte[] byArray, ASN1Set aSN1Set) {
        this(set, byArray);
    }

    public static class Parser
    extends ASN1Parser<ASN1Set> {
        public Parser(ASN1Decoder decoder) {
            super(decoder);
        }

        @Override
        public ASN1Set parse(ASN1Tag<ASN1Set> asn1Tag, byte[] value) throws ASN1ParseException {
            HashSet<ASN1Object> asn1Objects = new HashSet<ASN1Object>();
            try {
                Throwable throwable = null;
                Object var5_7 = null;
                try (ASN1InputStream stream = new ASN1InputStream(this.decoder, value);){
                    for (ASN1Object asn1Object : stream) {
                        asn1Objects.add(asn1Object);
                    }
                }
                catch (Throwable throwable2) {
                    if (throwable == null) {
                        throwable = throwable2;
                    } else if (throwable != throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            catch (IOException e) {
                throw new ASN1ParseException(e, "Could not parse ASN.1 SET contents.", new Object[0]);
            }
            return new ASN1Set(asn1Objects, value, null);
        }
    }

    public static class Serializer
    extends ASN1Serializer<ASN1Set> {
        public Serializer(ASN1Encoder encoder) {
            super(encoder);
        }

        @Override
        public int serializedLength(ASN1Set asn1Object) throws IOException {
            if (asn1Object.bytes == null) {
                this.calculateBytes(asn1Object);
            }
            return asn1Object.bytes.length;
        }

        private void calculateBytes(ASN1Set asn1Object) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ASN1OutputStream asn1OutputStream = new ASN1OutputStream(this.encoder, out);
            for (ASN1Object object : asn1Object) {
                asn1OutputStream.writeObject(object);
            }
            asn1Object.bytes = out.toByteArray();
        }

        @Override
        public void serialize(ASN1Set asn1Object, ASN1OutputStream stream) throws IOException {
            if (asn1Object.bytes != null) {
                stream.write(asn1Object.bytes);
            } else {
                for (ASN1Object object : asn1Object) {
                    stream.writeObject(object);
                }
            }
        }
    }
}

