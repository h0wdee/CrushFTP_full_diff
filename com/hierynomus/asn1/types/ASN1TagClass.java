/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.types;

public enum ASN1TagClass {
    Universal(0),
    Application(64),
    ContextSpecific(128),
    Private(192);

    private int value;

    private ASN1TagClass(int value) {
        this.value = value;
    }

    public static ASN1TagClass parseClass(byte tagByte) {
        int classValue = tagByte & 0xC0;
        ASN1TagClass[] aSN1TagClassArray = ASN1TagClass.values();
        int n = aSN1TagClassArray.length;
        int n2 = 0;
        while (n2 < n) {
            ASN1TagClass asn1TagClass = aSN1TagClassArray[n2];
            if (asn1TagClass.value == classValue) {
                return asn1TagClass;
            }
            ++n2;
        }
        throw new IllegalStateException("Could not parse ASN.1 Tag Class (should be impossible)");
    }

    public int getValue() {
        return this.value;
    }
}

