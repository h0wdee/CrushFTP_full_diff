/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.spnego;

import com.hierynomus.asn1.types.constructed.ASN1TaggedObject;
import com.hierynomus.spnego.NegTokenInit;
import com.hierynomus.spnego.SpnegoException;

public class NegTokenInit2
extends NegTokenInit {
    @Override
    protected void parseTagged(ASN1TaggedObject asn1TaggedObject) throws SpnegoException {
        if (asn1TaggedObject.getObject().toString().contains("not_defined_in_RFC4178@please_ignore")) {
            return;
        }
        switch (asn1TaggedObject.getTagNo()) {
            case 0: {
                this.readMechTypeList(asn1TaggedObject.getObject());
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                this.readMechToken(asn1TaggedObject.getObject());
                break;
            }
            case 3: {
                break;
            }
            case 4: {
                break;
            }
            default: {
                throw new SpnegoException("Unknown Object Tag " + asn1TaggedObject.getTagNo() + " encountered.");
            }
        }
    }
}

