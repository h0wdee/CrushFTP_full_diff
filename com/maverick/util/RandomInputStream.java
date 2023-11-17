/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class RandomInputStream
extends InputStream {
    long totalDataAmount;
    int maximumBlockSize;
    Random r = new Random();
    private MessageDigest digest;
    boolean randomBlock;

    public RandomInputStream(int maximumBlockSize, long totalDataAmount, boolean randomBlock) throws NoSuchAlgorithmException {
        this.maximumBlockSize = maximumBlockSize;
        this.totalDataAmount = totalDataAmount;
        this.randomBlock = randomBlock;
        this.digest = MessageDigest.getInstance("MD5");
    }

    @Override
    public int read(byte[] buf, int off, int len) {
        if (this.totalDataAmount == 0L) {
            return -1;
        }
        int max = Math.min(len, this.maximumBlockSize);
        if (this.totalDataAmount < (long)max) {
            max = (int)this.totalDataAmount;
        }
        int s = max;
        if (this.randomBlock && (s = this.r.nextInt(max)) == 0) {
            s = max;
        }
        byte[] b = new byte[s];
        this.r.nextBytes(b);
        this.digest.update(b);
        System.arraycopy(b, 0, buf, off, b.length);
        this.totalDataAmount -= (long)b.length;
        return b.length;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (this.read(b) > 0) {
            return b[1] & 0xFF;
        }
        return -1;
    }

    public byte[] digest() {
        return this.digest.digest();
    }
}

