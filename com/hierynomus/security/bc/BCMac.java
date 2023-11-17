/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.crypto.CipherParameters
 *  org.bouncycastle.crypto.Digest
 *  org.bouncycastle.crypto.Mac
 *  org.bouncycastle.crypto.digests.MD5Digest
 *  org.bouncycastle.crypto.digests.SHA256Digest
 *  org.bouncycastle.crypto.macs.HMac
 *  org.bouncycastle.crypto.params.KeyParameter
 */
package com.hierynomus.security.bc;

import com.hierynomus.protocol.commons.Factory;
import com.hierynomus.security.Mac;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

public class BCMac
implements Mac {
    private static Map<String, Factory<org.bouncycastle.crypto.Mac>> lookup = new HashMap<String, Factory<org.bouncycastle.crypto.Mac>>();
    private final org.bouncycastle.crypto.Mac mac;

    BCMac(String name) {
        this.mac = this.getMacFactory(name).create();
    }

    private Factory<org.bouncycastle.crypto.Mac> getMacFactory(String name) {
        Factory<org.bouncycastle.crypto.Mac> macFactory = lookup.get(name.toUpperCase());
        if (macFactory == null) {
            throw new IllegalArgumentException("No Mac defined for " + name);
        }
        return macFactory;
    }

    @Override
    public void init(byte[] key) {
        this.mac.init((CipherParameters)new KeyParameter(key));
    }

    @Override
    public void update(byte b) {
        this.mac.update(b);
    }

    @Override
    public void update(byte[] array) {
        this.mac.update(array, 0, array.length);
    }

    @Override
    public void update(byte[] array, int offset, int length) {
        this.mac.update(array, offset, length);
    }

    @Override
    public byte[] doFinal() {
        byte[] output = new byte[this.mac.getMacSize()];
        this.mac.doFinal(output, 0);
        return output;
    }

    @Override
    public void reset() {
        this.mac.reset();
    }

    static {
        lookup.put("HMACSHA256", new Factory<org.bouncycastle.crypto.Mac>(){

            @Override
            public org.bouncycastle.crypto.Mac create() {
                return new HMac((Digest)new SHA256Digest());
            }
        });
        lookup.put("HMACMD5", new Factory<org.bouncycastle.crypto.Mac>(){

            @Override
            public org.bouncycastle.crypto.Mac create() {
                return new HMac((Digest)new MD5Digest());
            }
        });
    }
}

