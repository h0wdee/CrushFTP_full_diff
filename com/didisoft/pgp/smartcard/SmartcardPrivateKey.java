/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.openpgp.PGPPublicKey
 */
package com.didisoft.pgp.smartcard;

import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.smartcard.SmartcardException;
import com.didisoft.pgp.smartcard.SmartcardKeyStore;
import com.didisoft.pgp.smartcard.SmartcardKeyType;
import lw.bouncycastle.openpgp.PGPPublicKey;

public class SmartcardPrivateKey {
    private SmartcardKeyStore keyStore;
    private long keyId;

    SmartcardPrivateKey(SmartcardKeyStore smartcardKeyStore, byte[] byArray) {
        this.keyStore = smartcardKeyStore;
        byte[] byArray2 = byArray;
        this.keyId = (long)byArray2[byArray2.length - 8] << 56 | (long)byArray2[byArray2.length - 7] << 48 | (long)byArray2[byArray2.length - 6] << 40 | (long)byArray2[byArray2.length - 5] << 32 | (long)byArray2[byArray2.length - 4] << 24 | (long)byArray2[byArray2.length - 3] << 16 | (long)byArray2[byArray2.length - 2] << 8 | (long)byArray2[byArray2.length - 1];
    }

    public long getKeyID() {
        return this.keyId;
    }

    public String getKeyIdHex() {
        return KeyPairInformation.keyIdToHex(this.keyId);
    }

    public String getKeyIdLongHex() {
        return KeyPairInformation.keyIdToLongHex(this.keyId);
    }

    PGPPublicKey getPublicKey(SmartcardKeyType smartcardKeyType) throws SmartcardException, PGPException {
        return this.keyStore.getPublicKey(smartcardKeyType);
    }

    public byte[] generateSignature(byte[] byArray) throws SmartcardException {
        return this.keyStore.generateSignature(byArray);
    }

    public byte[] decipher(byte[] byArray) throws SmartcardException {
        return this.keyStore.decipher(byArray);
    }
}

