/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.BCPGInputStream
 *  lw.bouncycastle.bcpg.ExperimentalPacket
 *  lw.bouncycastle.openpgp.PGPCompressedData
 *  lw.bouncycastle.openpgp.PGPEncryptedDataList
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPLiteralData
 *  lw.bouncycastle.openpgp.PGPMarker
 *  lw.bouncycastle.openpgp.PGPObjectFactory
 *  lw.bouncycastle.openpgp.PGPOnePassSignature
 *  lw.bouncycastle.openpgp.PGPOnePassSignatureList
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureList
 *  lw.bouncycastle.openpgp.operator.KeyFingerPrintCalculator
 *  lw.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
 */
package com.didisoft.pgp.bc;

import com.didisoft.pgp.bc.BCFactory;
import com.didisoft.pgp.bc.PGP2xPBEEncryptedData;
import com.didisoft.pgp.bc.ReflectionUtils;
import com.didisoft.pgp.bc.UnknownKeyPacketsException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import lw.bouncycastle.bcpg.BCPGInputStream;
import lw.bouncycastle.bcpg.ExperimentalPacket;
import lw.bouncycastle.openpgp.PGPCompressedData;
import lw.bouncycastle.openpgp.PGPEncryptedDataList;
import lw.bouncycastle.openpgp.PGPException;
import lw.bouncycastle.openpgp.PGPLiteralData;
import lw.bouncycastle.openpgp.PGPMarker;
import lw.bouncycastle.openpgp.PGPObjectFactory;
import lw.bouncycastle.openpgp.PGPOnePassSignature;
import lw.bouncycastle.openpgp.PGPOnePassSignatureList;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureList;
import lw.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import lw.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

public class PGPObjectFactory2
extends PGPObjectFactory {
    BCPGInputStream in;
    private BCFactory bcFactory = new BCFactory(false);
    private boolean loadingKey = false;

    public PGPObjectFactory2(InputStream inputStream) {
        super(inputStream, (KeyFingerPrintCalculator)new BcKeyFingerprintCalculator());
        this.in = new BCPGInputStream(inputStream);
    }

    public PGPObjectFactory2(byte[] byArray) {
        this(new ByteArrayInputStream(byArray));
    }

    public Object nextObject() throws IOException {
        try {
            switch (this.in.nextPacketTag()) {
                case -1: {
                    return null;
                }
                case 2: {
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    while (this.in.nextPacketTag() == 2) {
                        try {
                            arrayList.add(ReflectionUtils.callPrivateConstrtuctor(PGPSignature.class, new Object[]{this.in}, new Class[]{this.in.getClass()}));
                        }
                        catch (Exception exception) {
                            throw new IOException("can't create signature object: " + exception);
                        }
                    }
                    return new PGPSignatureList(arrayList.toArray(new PGPSignature[arrayList.size()]));
                }
                case 5: {
                    try {
                        return new PGPSecretKeyRing((InputStream)this.in, this.bcFactory.CreateKeyFingerPrintCalculator());
                    }
                    catch (PGPException pGPException) {
                        throw new IOException("can't create secret key object: " + (Object)((Object)pGPException));
                    }
                }
                case 6: {
                    return new PGPPublicKeyRing((InputStream)this.in, this.bcFactory.CreateKeyFingerPrintCalculator());
                }
                case 8: {
                    return new PGPCompressedData(this.in);
                }
                case 11: {
                    return new PGPLiteralData(this.in);
                }
                case 1: 
                case 3: {
                    return new PGPEncryptedDataList(this.in);
                }
                case 4: {
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    while (this.in.nextPacketTag() == 4) {
                        try {
                            arrayList.add(ReflectionUtils.callPrivateConstrtuctor(PGPOnePassSignature.class, new Object[]{this.in}, new Class[]{this.in.getClass()}));
                        }
                        catch (Exception exception) {
                            if (this.loadingKey && exception instanceof IOException) {
                                throw new UnknownKeyPacketsException("corrupted object in stream 4", exception);
                            }
                            throw new IOException("can't create one pass signature object: " + exception);
                        }
                    }
                    return new PGPOnePassSignatureList(arrayList.toArray(new PGPOnePassSignature[arrayList.size()]));
                }
                case 10: {
                    return new PGPMarker(this.in);
                }
                case 60: 
                case 61: 
                case 62: 
                case 63: {
                    return (ExperimentalPacket)this.in.readPacket();
                }
                case 9: {
                    return new PGP2xPBEEncryptedData(this.in);
                }
            }
        }
        catch (IOException iOException) {
            if (this.loadingKey) {
                throw new UnknownKeyPacketsException(iOException.getMessage(), iOException);
            }
            throw iOException;
        }
        if (this.loadingKey) {
            throw new UnknownKeyPacketsException("unknown object in stream " + this.in.nextPacketTag());
        }
        throw new IOException("unknown object in stream " + this.in.nextPacketTag());
    }

    public boolean isLoadingKey() {
        return this.loadingKey;
    }

    public void setLoadingKey(boolean bl) {
        this.loadingKey = bl;
    }
}

