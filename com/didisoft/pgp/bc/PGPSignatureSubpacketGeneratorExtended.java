/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.SignatureSubpacket
 *  lw.bouncycastle.bcpg.sig.Exportable
 *  lw.bouncycastle.bcpg.sig.IssuerKeyID
 *  lw.bouncycastle.bcpg.sig.KeyExpirationTime
 *  lw.bouncycastle.bcpg.sig.KeyFlags
 *  lw.bouncycastle.bcpg.sig.NotationData
 *  lw.bouncycastle.bcpg.sig.PreferredAlgorithms
 *  lw.bouncycastle.bcpg.sig.PrimaryUserID
 *  lw.bouncycastle.bcpg.sig.Revocable
 *  lw.bouncycastle.bcpg.sig.SignatureCreationTime
 *  lw.bouncycastle.bcpg.sig.SignatureExpirationTime
 *  lw.bouncycastle.bcpg.sig.SignerUserID
 *  lw.bouncycastle.bcpg.sig.TrustSignature
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureSubpacketGenerator
 *  lw.bouncycastle.openpgp.PGPSignatureSubpacketVector
 */
package com.didisoft.pgp.bc;

import com.didisoft.pgp.bc.RevocationKey;
import com.didisoft.pgp.bc.RevocationReason;
import com.didisoft.pgp.bc.sig.EmbeddedSignature;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lw.bouncycastle.bcpg.SignatureSubpacket;
import lw.bouncycastle.bcpg.sig.Exportable;
import lw.bouncycastle.bcpg.sig.IssuerKeyID;
import lw.bouncycastle.bcpg.sig.KeyExpirationTime;
import lw.bouncycastle.bcpg.sig.KeyFlags;
import lw.bouncycastle.bcpg.sig.NotationData;
import lw.bouncycastle.bcpg.sig.PreferredAlgorithms;
import lw.bouncycastle.bcpg.sig.PrimaryUserID;
import lw.bouncycastle.bcpg.sig.Revocable;
import lw.bouncycastle.bcpg.sig.SignatureCreationTime;
import lw.bouncycastle.bcpg.sig.SignatureExpirationTime;
import lw.bouncycastle.bcpg.sig.SignerUserID;
import lw.bouncycastle.bcpg.sig.TrustSignature;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import lw.bouncycastle.openpgp.PGPSignatureSubpacketVector;

public class PGPSignatureSubpacketGeneratorExtended
extends PGPSignatureSubpacketGenerator {
    List list = new ArrayList();

    public void setRevocable(boolean bl, boolean bl2) {
        this.list.add(new Revocable(bl, bl2));
    }

    public void setExportable(boolean bl, boolean bl2) {
        this.list.add(new Exportable(bl, bl2));
    }

    public void setTrust(boolean bl, int n, int n2) {
        this.list.add(new TrustSignature(bl, n, n2));
    }

    public void setKeyExpirationTime(boolean bl, long l) {
        this.list.add(new KeyExpirationTime(bl, l));
    }

    public void setSignatureExpirationTime(boolean bl, long l) {
        this.list.add(new SignatureExpirationTime(bl, l));
    }

    public void setSignatureCreationTime(boolean bl, Date date) {
        this.list.add(new SignatureCreationTime(bl, date));
    }

    public void setPreferredHashAlgorithms(boolean bl, int[] nArray) {
        this.list.add(new PreferredAlgorithms(21, bl, nArray));
    }

    public void setPreferredSymmetricAlgorithms(boolean bl, int[] nArray) {
        this.list.add(new PreferredAlgorithms(11, bl, nArray));
    }

    public void setPreferredCompressionAlgorithms(boolean bl, int[] nArray) {
        this.list.add(new PreferredAlgorithms(22, bl, nArray));
    }

    public void setKeyFlags(boolean bl, int n) {
        this.list.add(new KeyFlags(bl, n));
    }

    public void setSignerUserID(boolean bl, String string) {
        if (string == null) {
            throw new IllegalArgumentException("attempt to set null SignerUserID");
        }
        this.list.add(new SignerUserID(bl, string));
    }

    public void setEmbeddedSignature(boolean bl, PGPSignature pGPSignature) throws IOException {
        byte[] byArray = pGPSignature.getEncoded();
        byte[] byArray2 = byArray.length - 1 > 256 ? new byte[byArray.length - 3] : new byte[byArray.length - 2];
        System.arraycopy(byArray, byArray.length - byArray2.length, byArray2, 0, byArray2.length);
        this.list.add(new EmbeddedSignature(bl, byArray2));
    }

    public void setPrimaryUserID(boolean bl, boolean bl2) {
        this.list.add(new PrimaryUserID(bl, bl2));
    }

    public void setNotationData(boolean bl, boolean bl2, String string, String string2) {
        this.list.add(new NotationData(bl, bl2, string, string2));
    }

    public PGPSignatureSubpacketVector generate() {
        SignatureSubpacket[] signatureSubpacketArray = this.list.toArray(new SignatureSubpacket[this.list.size()]);
        Object var2_2 = null;
        try {
            Class<PGPSignatureSubpacketVector> clazz = PGPSignatureSubpacketVector.class;
            Constructor<?> constructor = null;
            Constructor<?>[] constructorArray = clazz.getDeclaredConstructors();
            for (int i = 0; i < constructorArray.length; ++i) {
                Class<?>[] classArray = constructorArray[i].getParameterTypes();
                if (classArray.length != 1 || !classArray[0].isArray() || !classArray[0].getComponentType().equals(SignatureSubpacket.class)) continue;
                constructor = constructorArray[i];
                break;
            }
            constructor.setAccessible(true);
            var2_2 = constructor.newInstance(new Object[]{signatureSubpacketArray});
        }
        catch (InvocationTargetException invocationTargetException) {
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (InstantiationException instantiationException) {
            // empty catch block
        }
        return var2_2;
    }

    public void setRevocationReason(boolean bl, byte by, String string) {
        this.list.add(new RevocationReason(bl, by, string));
    }

    public void setRevocationKey(boolean bl, byte by, byte[] byArray) {
        this.list.add(new RevocationKey(bl, -128, by, byArray));
    }

    public void setIssuerKeyID(boolean bl, long l) {
        this.list.add(new IssuerKeyID(bl, l));
    }
}

