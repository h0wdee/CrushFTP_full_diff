/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.PublicKeyPacket
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPPublicKey
 */
package com.didisoft.pgp.bc;

import com.didisoft.pgp.bc.BCFactory;
import java.util.List;
import lw.bouncycastle.bcpg.PublicKeyPacket;
import lw.bouncycastle.openpgp.PGPException;
import lw.bouncycastle.openpgp.PGPPublicKey;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PGPPublicKeyExt
extends PGPPublicKey {
    private static BCFactory factory = new BCFactory(false);
    private List<Integer> preferredCyphers;
    private List<Integer> preferredHashes;
    private List<Integer> preferredCompressions;

    public PGPPublicKeyExt(PublicKeyPacket publicKeyPacket) throws PGPException {
        super(publicKeyPacket, factory.CreateKeyFingerPrintCalculator());
    }

    public List<Integer> getPreferredCyphers() {
        return this.preferredCyphers;
    }

    public void setPreferredCyphers(List<Integer> list) {
        this.preferredCyphers = list;
    }

    public List<Integer> getPreferredHashes() {
        return this.preferredHashes;
    }

    public void setPreferredHashes(List<Integer> list) {
        this.preferredHashes = list;
    }

    public List<Integer> getPreferredCompressions() {
        return this.preferredCompressions;
    }

    public void setPreferredCompressions(List<Integer> list) {
        this.preferredCompressions = list;
    }
}

