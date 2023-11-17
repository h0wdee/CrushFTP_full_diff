/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.inspect;

import com.didisoft.pgp.KeyPairInformation;
import java.util.Date;

public class SignatureItem {
    private long keyId;
    private Date signatureTime;
    private String userId = "";

    public SignatureItem(long l, Date date) {
        this.keyId = l;
        this.signatureTime = date;
    }

    public long getKeyId() {
        return this.keyId;
    }

    public String getKeyIdHex() {
        return KeyPairInformation.keyId2Hex(this.getKeyId());
    }

    public Date getSignatureTime() {
        return this.signatureTime;
    }

    void setKeyId(long l) {
        this.keyId = l;
    }

    void setSignatureTime(Date date) {
        this.signatureTime = date;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String string) {
        this.userId = string;
    }
}

