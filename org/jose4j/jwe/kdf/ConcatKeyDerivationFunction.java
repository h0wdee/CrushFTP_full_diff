/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe.kdf;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.HashUtil;

public class ConcatKeyDerivationFunction {
    private int digestLength;
    private MessageDigest messageDigest;

    public ConcatKeyDerivationFunction(String hashAlgoritm) {
        this.messageDigest = HashUtil.getMessageDigest(hashAlgoritm);
        this.init();
    }

    public ConcatKeyDerivationFunction(String hashAlgoritm, String provider) {
        this.messageDigest = HashUtil.getMessageDigest(hashAlgoritm, provider);
        this.init();
    }

    private void init() {
        this.digestLength = ByteUtil.bitLength(this.messageDigest.getDigestLength());
    }

    public byte[] kdf(byte[] sharedSecret, int keydatalen, byte[] algorithmId, byte[] partyUInfo, byte[] partyVInfo, byte[] suppPubInfo, byte[] suppPrivInfo) {
        if (this.traceLog()) {
            StringBuilder msg = new StringBuilder();
            msg.append("KDF:").append("\n");
            msg.append("  z: ").append(ByteUtil.toDebugString(sharedSecret)).append("\n");
            msg.append("  keydatalen: ").append(keydatalen);
            msg.append("  algorithmId: ").append(ByteUtil.toDebugString(algorithmId)).append("\n");
            msg.append("  partyUInfo: ").append(ByteUtil.toDebugString(partyUInfo)).append("\n");
            msg.append("  partyVInfo: ").append(ByteUtil.toDebugString(partyVInfo)).append("\n");
            msg.append("  suppPubInfo: ").append(ByteUtil.toDebugString(suppPubInfo)).append("\n");
            msg.append("  suppPrivInfo: ").append(ByteUtil.toDebugString(suppPrivInfo));
        }
        byte[] otherInfo = ByteUtil.concat(algorithmId, partyUInfo, partyVInfo, suppPubInfo, suppPrivInfo);
        return this.kdf(sharedSecret, keydatalen, otherInfo);
    }

    public byte[] kdf(byte[] sharedSecret, int keydatalen, byte[] otherInfo) {
        long reps = this.getReps(keydatalen);
        this.traceLog();
        ByteArrayOutputStream derivedByteOutputStream = new ByteArrayOutputStream();
        int i = 1;
        while ((long)i <= reps) {
            byte[] counterBytes = ByteUtil.getBytes(i);
            this.traceLog();
            this.messageDigest.update(counterBytes);
            this.messageDigest.update(sharedSecret);
            this.messageDigest.update(otherInfo);
            byte[] digest = this.messageDigest.digest();
            derivedByteOutputStream.write(digest, 0, digest.length);
            ++i;
        }
        int keyDateLenInBytes = ByteUtil.byteLength(keydatalen);
        byte[] derivedKeyMaterial = derivedByteOutputStream.toByteArray();
        if (derivedKeyMaterial.length != keyDateLenInBytes) {
            byte[] newKeyMaterial = ByteUtil.subArray(derivedKeyMaterial, 0, keyDateLenInBytes);
            derivedKeyMaterial = newKeyMaterial;
        }
        return derivedKeyMaterial;
    }

    long getReps(int keydatalen) {
        double repsD = (float)keydatalen / (float)this.digestLength;
        repsD = Math.ceil(repsD);
        return (int)repsD;
    }

    private boolean traceLog() {
        return false;
    }
}

