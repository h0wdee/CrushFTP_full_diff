/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.spnego;

import com.hierynomus.asn1.ASN1InputStream;
import com.hierynomus.asn1.encodingrules.ASN1Decoder;
import com.hierynomus.asn1.encodingrules.der.DERDecoder;
import com.hierynomus.asn1.types.ASN1Object;
import com.hierynomus.asn1.types.ASN1Tag;
import com.hierynomus.asn1.types.ASN1TagClass;
import com.hierynomus.asn1.types.constructed.ASN1Sequence;
import com.hierynomus.asn1.types.constructed.ASN1TaggedObject;
import com.hierynomus.asn1.types.primitive.ASN1ObjectIdentifier;
import com.hierynomus.asn1.types.string.ASN1OctetString;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.commons.buffer.Endian;
import com.hierynomus.spnego.ObjectIdentifiers;
import com.hierynomus.spnego.SpnegoException;
import com.hierynomus.spnego.SpnegoToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NegTokenInit
extends SpnegoToken {
    static final String ADS_IGNORE_PRINCIPAL = "not_defined_in_RFC4178@please_ignore";
    private List<ASN1ObjectIdentifier> mechTypes = new ArrayList<ASN1ObjectIdentifier>();
    protected byte[] mechToken;

    public NegTokenInit() {
        super(0, "NegTokenInit");
    }

    public void write(Buffer<?> buffer) throws SpnegoException {
        try {
            ArrayList<ASN1Object> negTokenInit = new ArrayList<ASN1Object>();
            this.addMechTypeList(negTokenInit);
            this.addMechToken(negTokenInit);
            this.writeGss(buffer, new ASN1Sequence(negTokenInit));
        }
        catch (IOException e) {
            throw new SpnegoException("Unable to write NegTokenInit", e);
        }
    }

    public NegTokenInit read(byte[] bytes) throws SpnegoException {
        return this.read(new Buffer.PlainBuffer(bytes, Endian.LE));
    }

    private NegTokenInit read(Buffer<?> buffer) throws SpnegoException {
        try (ASN1InputStream is = new ASN1InputStream((ASN1Decoder)new DERDecoder(), buffer.asInputStream());){
            ASN1TaggedObject applicationSpecific = (ASN1TaggedObject)is.readObject();
            if (applicationSpecific.getTag().getAsn1TagClass() != ASN1TagClass.APPLICATION) {
                throw new SpnegoException("Incorrect GSS-API ASN.1 token received, expected to find an [APPLICATION 0], not: " + applicationSpecific);
            }
            ASN1Sequence implicitSequence = applicationSpecific.getObject(ASN1Tag.SEQUENCE);
            ASN1Object spnegoOid = implicitSequence.get(0);
            if (!(spnegoOid instanceof ASN1ObjectIdentifier)) {
                throw new SpnegoException("Expected to find the SPNEGO OID (" + ObjectIdentifiers.SPNEGO + "), not: " + spnegoOid);
            }
            this.parseSpnegoToken(implicitSequence.get(1));
        }
        catch (IOException ioe) {
            throw new SpnegoException("Could not read NegTokenInit from buffer", ioe);
        }
        return this;
    }

    @Override
    protected void parseTagged(ASN1TaggedObject asn1TaggedObject) throws SpnegoException {
        if (asn1TaggedObject.getObject().toString().contains(ADS_IGNORE_PRINCIPAL)) {
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
            default: {
                throw new SpnegoException("Unknown Object Tag " + asn1TaggedObject.getTagNo() + " encountered.");
            }
        }
    }

    void readMechToken(ASN1Object mechToken) throws SpnegoException {
        if (!(mechToken instanceof ASN1OctetString)) {
            throw new SpnegoException("Expected the MechToken (OCTET_STRING) contents, not: " + mechToken);
        }
        this.mechToken = ((ASN1OctetString)mechToken).getValue();
    }

    void readMechTypeList(ASN1Object sequence) throws SpnegoException {
        if (!(sequence instanceof ASN1Sequence)) {
            throw new SpnegoException("Expected the MechTypeList (SEQUENCE) contents, not: " + sequence);
        }
        for (ASN1Object mechType : (ASN1Sequence)sequence) {
            if (!(mechType instanceof ASN1ObjectIdentifier)) {
                throw new SpnegoException("Expected a MechType (OBJECT IDENTIFIER) as contents of the MechTypeList, not: " + mechType);
            }
            this.mechTypes.add((ASN1ObjectIdentifier)mechType);
        }
    }

    private void addMechToken(List<ASN1Object> negTokenInit) {
        if (this.mechToken != null && this.mechToken.length > 0) {
            ASN1TaggedObject token = new ASN1TaggedObject(ASN1Tag.contextSpecific(2).constructed(), new ASN1OctetString(this.mechToken), true);
            negTokenInit.add(token);
        }
    }

    private void addMechTypeList(List<ASN1Object> negTokenInit) {
        if (this.mechTypes.size() > 0) {
            ArrayList<ASN1Object> supportedMechVector = new ArrayList<ASN1Object>(this.mechTypes);
            negTokenInit.add(new ASN1TaggedObject(ASN1Tag.contextSpecific(0).constructed(), new ASN1Sequence(supportedMechVector), true));
        }
    }

    public void addSupportedMech(ASN1ObjectIdentifier oid) {
        this.mechTypes.add(oid);
    }

    public void setMechToken(byte[] mechToken) {
        this.mechToken = mechToken;
    }

    public List<ASN1ObjectIdentifier> getSupportedMechTypes() {
        return this.mechTypes;
    }
}

