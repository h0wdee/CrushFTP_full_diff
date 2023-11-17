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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ASN1Sequence
extends ASN1Object<List<ASN1Object>>
implements ASN1Constructed {
    private final List<ASN1Object> objects;
    private byte[] bytes;

    private ASN1Sequence(List<ASN1Object> objects, byte[] bytes) {
        super(ASN1Tag.SEQUENCE);
        this.objects = objects;
        this.bytes = bytes;
    }

    public ASN1Sequence(List<ASN1Object> objects) {
        super(ASN1Tag.SEQUENCE);
        this.objects = objects;
    }

    @Override
    public List<ASN1Object> getValue() {
        return new ArrayList<ASN1Object>(this.objects);
    }

    @Override
    public Iterator<ASN1Object> iterator() {
        return new ArrayList<ASN1Object>(this.objects).iterator();
    }

    public int size() {
        return this.objects.size();
    }

    public ASN1Object get(int i) {
        return this.objects.get(i);
    }

    /* synthetic */ ASN1Sequence(List list, byte[] byArray, ASN1Sequence aSN1Sequence) {
        this(list, byArray);
    }

    public static class Parser
    extends ASN1Parser<ASN1Sequence> {
        public Parser(ASN1Decoder decoder) {
            super(decoder);
        }

        @Override
        public ASN1Sequence parse(ASN1Tag<ASN1Sequence> asn1Tag, byte[] value) throws ASN1ParseException {
            ArrayList<ASN1Object> list = new ArrayList<ASN1Object>();
            try {
                Throwable throwable = null;
                Object var5_7 = null;
                try (ASN1InputStream stream = new ASN1InputStream(this.decoder, value);){
                    for (ASN1Object asn1Object : stream) {
                        list.add(asn1Object);
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
                throw new ASN1ParseException(e, "Unable to parse the ASN.1 SEQUENCE contents.", new Object[0]);
            }
            return new ASN1Sequence(list, value, null);
        }
    }

    public static class Serializer
    extends ASN1Serializer<ASN1Sequence> {
        public Serializer(ASN1Encoder encoder) {
            super(encoder);
        }

        @Override
        public int serializedLength(ASN1Sequence asn1Object) throws IOException {
            if (asn1Object.bytes == null) {
                this.calculateBytes(asn1Object);
            }
            return asn1Object.bytes.length;
        }

        private void calculateBytes(ASN1Sequence asn1Object) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ASN1OutputStream asn1OutputStream = new ASN1OutputStream(this.encoder, out);
            for (ASN1Object object : asn1Object) {
                asn1OutputStream.writeObject(object);
            }
            asn1Object.bytes = out.toByteArray();
        }

        @Override
        public void serialize(ASN1Sequence asn1Object, ASN1OutputStream stream) throws IOException {
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

