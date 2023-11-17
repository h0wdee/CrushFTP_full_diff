/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.crypto.Digest
 *  org.bouncycastle.crypto.digests.MD4Digest
 *  org.bouncycastle.crypto.digests.SHA256Digest
 */
package com.hierynomus.security.bc;

import com.hierynomus.protocol.commons.Factory;
import com.hierynomus.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

public class BCMessageDigest
implements MessageDigest {
    private static Map<String, Factory<Digest>> lookup = new HashMap<String, Factory<Digest>>();
    private final Digest digest;

    BCMessageDigest(String name) {
        this.digest = this.getDigest(name);
    }

    private Digest getDigest(String name) {
        Factory<Digest> digestFactory = lookup.get(name);
        if (digestFactory == null) {
            throw new IllegalArgumentException("No MessageDigest " + name + " defined in BouncyCastle");
        }
        return digestFactory.create();
    }

    @Override
    public void update(byte[] bytes) {
        this.digest.update(bytes, 0, bytes.length);
    }

    @Override
    public byte[] digest() {
        byte[] output = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(output, 0);
        return output;
    }

    @Override
    public void reset() {
        this.digest.reset();
    }

    static {
        lookup.put("SHA256", new Factory<Digest>(){

            @Override
            public Digest create() {
                return new SHA256Digest();
            }
        });
        lookup.put("MD4", new Factory<Digest>(){

            @Override
            public Digest create() {
                return new MD4Digest();
            }
        });
    }
}

