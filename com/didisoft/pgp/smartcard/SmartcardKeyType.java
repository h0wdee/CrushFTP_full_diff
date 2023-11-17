/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.smartcard;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum SmartcardKeyType {
    SignatureKey(-74),
    DecryptionKey(-72),
    AuthenticationKey(-92);

    private byte val = 0;

    private SmartcardKeyType(byte by) {
        this.val = by;
    }

    public byte value() {
        return this.value();
    }
}

